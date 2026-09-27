package io.github.rwx

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.SystemClock
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import androidx.core.net.toUri
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap

internal class AndroidSafAccess(context: Context) : SafPlatformAccess {
    private val resolver: ContentResolver = context.applicationContext.contentResolver
    private val trees = ConcurrentHashMap<String, Uri>()
    // 两级内存缓存（TTL + LRU 有界，防止泄漏）：doc 缓存 pathKey -> SafDocument，
    // children 缓存 treeUri+parentId -> List<SafDocument>。存储用 ConcurrentHashMap，
    // LRU 顺序表的所有访问必须在对应 lock 下同步。
    private val docCache = ConcurrentHashMap<String, CachedDoc>()
    private val childrenCache = ConcurrentHashMap<String, CachedChildren>()
    private val docLruLock = Any()
    private val childrenLruLock = Any()
    private val docLru = object : LinkedHashMap<String, Unit>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Unit>): Boolean {
            if (size > MAX_DOC_ENTRIES) {
                docCache.remove(eldest.key)
                return true
            }
            return false
        }
    }
    private val childrenLru = object : LinkedHashMap<String, Unit>(32, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Unit>): Boolean {
            if (size > MAX_CHILDREN_DIRS) {
                childrenCache.remove(eldest.key)
                return true
            }
            return false
        }
    }

    override fun registerTree(uri: String): String? = runCatching {
        val treeUri = uri.toUri()
        DocumentsContract.getTreeDocumentId(treeUri)
        val key = "saf-${uri.hashCode().toUInt().toString(16)}${SafPlatformBridge.SAF_LINK_SUFFIX}"
        trees[key] = treeUri
        "/$key"
    }.getOrNull()

    override fun exists(path: String): Boolean =
        runCatching { resolve(path) != null }.getOrDefault(false)

    override fun isDirectory(path: String): Boolean =
        runCatching { resolve(path)?.mimeType == Document.MIME_TYPE_DIR }.getOrDefault(false)

    override fun createDirectory(path: String): Boolean {
        val parsed = parse(path) ?: return false
        ensureDirectory(parsed.treeUri, parsed.segments) ?: return false
        invalidateTree(parsed.treeUri)
        return true
    }

    override fun list(path: String): Array<String>? = runCatching {
        val parsed = parse(path) ?: return@runCatching null
        val directory = resolveParsed(parsed)?.takeIf { it.mimeType == Document.MIME_TYPE_DIR }
            ?: return@runCatching null
        cachedChildren(directory.treeUri, directory.documentId, parsed.segments)
            .map { it.name }
            .sorted()
            .toTypedArray()
    }.getOrNull()

    override fun size(path: String): Long =
        runCatching { resolve(path)?.size ?: -1L }.getOrDefault(-1L)

    override fun lastModified(path: String): Long =
        runCatching { resolve(path)?.lastModified ?: 0L }.getOrDefault(0L)

    override fun openInput(path: String): InputStream? = runCatching {
        resolve(path)?.let { resolver.openInputStream(it.documentUri) }
    }.getOrNull()

    override fun openOutput(path: String, append: Boolean): OutputStream? {
        val parsed = parse(path) ?: return null
        val fileName = parsed.segments.lastOrNull() ?: return null
        val parent = ensureDirectory(parsed.treeUri, parsed.segments.dropLast(1)) ?: return null
        val existing = findChild(parent.treeUri, parent.documentId, fileName)
        val file = existing ?: createDocument(
            parent,
            fileName,
            "application/octet-stream",
            parsed.segments.dropLast(1),
        ) ?: return null
        val stream = runCatching {
            resolver.openOutputStream(file.documentUri, if (append) "wa" else "w")
        }.getOrNull() ?: return null
        invalidateTree(parsed.treeUri)
        return stream
    }

    override fun rename(sourcePath: String, targetPath: String): Boolean {
        val source = resolve(sourcePath) ?: return false
        val target = parse(targetPath) ?: return false
        val targetName = target.segments.lastOrNull() ?: return false
        val targetParentSegments = target.segments.dropLast(1)
        if (source.parentSegments != targetParentSegments) return false
        val ok = runCatching {
            DocumentsContract.renameDocument(resolver, source.documentUri, targetName) != null
        }.getOrDefault(false)
        if (ok) {
            invalidateTree(source.treeUri)
            invalidateTree(target.treeUri)
        }
        return ok
    }

    override fun delete(path: String): Boolean {
        val document = resolve(path) ?: return false
        val ok = runCatching {
            DocumentsContract.deleteDocument(resolver, document.documentUri)
        }.getOrDefault(false)
        if (ok) invalidateTree(document.treeUri)
        return ok
    }

    private fun resolve(path: String): SafDocument? {
        val parsed = parse(path) ?: return null
        return resolveParsed(parsed)
    }

    private fun resolveParsed(parsed: ParsedSafPath): SafDocument? {
        val treeKey = parsed.treeUri.toString()
        val rootId = DocumentsContract.getTreeDocumentId(parsed.treeUri)
        val rootKey = "$treeKey|"
        var current = getCachedDoc(rootKey)
            ?: queryDocument(parsed.treeUri, rootId, emptyList())?.also { putCachedDoc(rootKey, it) }
            ?: return null
        for ((index, segment) in parsed.segments.withIndex()) {
            val childKey = "$treeKey|" + parsed.segments.take(index + 1).joinToString("/")
            val hit = getCachedDoc(childKey)
            if (hit != null) {
                current = hit
                continue
            }
            val found = cachedChildren(parsed.treeUri, current.documentId, parsed.segments.take(index))
                .firstOrNull { it.name == segment } ?: return null
            putCachedDoc(childKey, found)
            current = found
        }
        return current
    }

    private fun ensureDirectory(treeUri: Uri, segments: List<String>): SafDocument? {
        val rootId = DocumentsContract.getTreeDocumentId(treeUri)
        var current = queryDocument(treeUri, rootId, emptyList()) ?: return null
        segments.forEachIndexed { index, segment ->
            val existing = findChild(treeUri, current.documentId, segment, segments.take(index))
            current = when {
                existing == null -> createDocument(
                    current,
                    segment,
                    Document.MIME_TYPE_DIR,
                    segments.take(index),
                ) ?: return null

                existing.mimeType == Document.MIME_TYPE_DIR -> existing
                else -> return null
            }
        }
        return current
    }

    private fun createDocument(
        parent: SafDocument,
        name: String,
        mimeType: String,
        parentSegments: List<String>,
    ): SafDocument? {
        val uri = runCatching {
            DocumentsContract.createDocument(resolver, parent.documentUri, mimeType, name)
        }.getOrNull() ?: return null
        val id = runCatching { DocumentsContract.getDocumentId(uri) }.getOrNull() ?: return null
        val doc = queryDocument(parent.treeUri, id, parentSegments) ?: return null
        invalidateTree(parent.treeUri)
        return doc
    }

    private fun findChild(
        treeUri: Uri,
        parentId: String,
        name: String,
        parentSegments: List<String> = emptyList(),
    ): SafDocument? = cachedChildren(treeUri, parentId, parentSegments).firstOrNull { it.name == name }

    private fun getCachedDoc(key: String): SafDocument? {
        val entry = docCache[key] ?: return null
        if (SystemClock.uptimeMillis() - entry.cachedAt > CACHE_TTL_MS) {
            docCache.remove(key)
            synchronized(docLruLock) { docLru.remove(key) }
            return null
        }
        synchronized(docLruLock) { docLru[key] = Unit }
        return entry.doc
    }

    private fun putCachedDoc(key: String, doc: SafDocument) {
        docCache[key] = CachedDoc(doc, SystemClock.uptimeMillis())
        synchronized(docLruLock) { docLru[key] = Unit }
    }

    private fun cachedChildren(
        treeUri: Uri,
        parentId: String,
        parentSegments: List<String>,
    ): List<SafDocument> {
        val key = treeUri.toString() + "|" + parentId
        childrenCache[key]?.let { entry ->
            if (SystemClock.uptimeMillis() - entry.cachedAt <= CACHE_TTL_MS) {
                synchronized(childrenLruLock) { childrenLru[key] = Unit }
                return entry.children
            }
            childrenCache.remove(key)
            synchronized(childrenLruLock) { childrenLru.remove(key) }
        }
        val fresh = queryChildren(treeUri, parentId, parentSegments)
        childrenCache[key] = CachedChildren(fresh, SystemClock.uptimeMillis())
        synchronized(childrenLruLock) { childrenLru[key] = Unit }
        val treeKey = treeUri.toString()
        fresh.forEach { child ->
            if (!child.name.contains('/')) {
                putCachedDoc("$treeKey|" + (parentSegments + child.name).joinToString("/"), child)
            }
        }
        return fresh
    }

    // 粗粒度失效：只清该 treeUri 名下缓存，不碰其他树。写操作远少于读扫描，简单正确优先。
    private fun invalidateTree(treeUri: Uri) {
        val prefix = treeUri.toString() + "|"
        docCache.keys.removeIf { it.startsWith(prefix) }
        childrenCache.keys.removeIf { it.startsWith(prefix) }
        synchronized(docLruLock) { docLru.keys.removeIf { it.startsWith(prefix) } }
        synchronized(childrenLruLock) { childrenLru.keys.removeIf { it.startsWith(prefix) } }
    }

    private fun queryDocument(treeUri: Uri, documentId: String, parentSegments: List<String>): SafDocument? {
        val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        return resolver.query(uri, PROJECTION, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            cursor.toSafDocument(treeUri, parentSegments)
        }
    }

    private fun queryChildren(
        treeUri: Uri,
        parentId: String,
        parentSegments: List<String> = emptyList(),
    ): List<SafDocument> {
        val uri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentId)
        return resolver.query(uri, PROJECTION, null, null, null)?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.toSafDocument(treeUri, parentSegments))
            }
        } ?: emptyList()
    }

    private fun android.database.Cursor.toSafDocument(
        treeUri: Uri,
        parentSegments: List<String>,
    ): SafDocument {
        val id = getString(0)
        return SafDocument(
            treeUri = treeUri,
            documentId = id,
            documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, id),
            name = getString(1),
            mimeType = getString(2),
            size = if (isNull(3)) 0L else getLong(3),
            lastModified = if (isNull(4)) 0L else getLong(4),
            parentSegments = parentSegments,
        )
    }

    private fun parse(path: String): ParsedSafPath? {
        val normalized = path.replace('\\', '/')
        val markerIndex = normalized.indexOf(SafPlatformBridge.SAF_LINK_SUFFIX)
        if (markerIndex < 0) return null
        val key = normalized.substring(0, markerIndex + SafPlatformBridge.SAF_LINK_SUFFIX.length).substringAfterLast('/')
        val treeUri = trees[key] ?: return null
        val relative = normalized.substring(markerIndex + SafPlatformBridge.SAF_LINK_SUFFIX.length).trim('/')
        val segments = relative.split('/').filter { it.isNotBlank() && it != "." }
        if (segments.any { it == ".." }) return null
        return ParsedSafPath(treeUri, segments)
    }

    private data class ParsedSafPath(val treeUri: Uri, val segments: List<String>)

    private data class SafDocument(
        val treeUri: Uri,
        val documentId: String,
        val documentUri: Uri,
        val name: String,
        val mimeType: String,
        val size: Long,
        val lastModified: Long,
        val parentSegments: List<String>,
    )

    private data class CachedDoc(val doc: SafDocument, val cachedAt: Long)

    private data class CachedChildren(val children: List<SafDocument>, val cachedAt: Long)

    companion object {
        private val PROJECTION = arrayOf(
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_SIZE,
            Document.COLUMN_LAST_MODIFIED,
        )
        private const val CACHE_TTL_MS = 4000L
        private const val MAX_DOC_ENTRIES = 512
        private const val MAX_CHILDREN_DIRS = 128
    }
}
