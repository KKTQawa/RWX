package io.github.rwx.ui.screen

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.util.zip.ZipFile

internal actual suspend fun readModPreviewBytes(path: String, entryName: String): ByteArray? = withContext(Dispatchers.IO) {
    runCatching {
        ZipFile(path).use { zip ->
            val entry = zip.getEntry(entryName) ?: return@use null
            if (entry.isDirectory || entry.size > MAX_PREVIEW_BYTES) return@use null
            zip.getInputStream(entry).use(::readPreviewBytes)
        }
    }.getOrNull()
}

internal actual suspend fun readResourcePreviewBytes(url: String): ByteArray? = withContext(Dispatchers.IO) {
    val parsed = runCatching { URI(url).toURL() }.getOrNull() ?: return@withContext null
    if (parsed.protocol != "http" && parsed.protocol != "https") return@withContext null
    val connection = runCatching { parsed.openConnection() as HttpURLConnection }.getOrNull() ?: return@withContext null
    try {
        connection.connectTimeout = PREVIEW_TIMEOUT_MILLIS
        connection.readTimeout = PREVIEW_TIMEOUT_MILLIS
        connection.instanceFollowRedirects = true
        if (connection.responseCode !in 200..299) return@withContext null
        if (connection.contentLengthLong > MAX_PREVIEW_BYTES) return@withContext null
        connection.inputStream.use(::readPreviewBytes)
    } catch (_: Exception) {
        null
    } finally {
        connection.disconnect()
    }
}

private fun readPreviewBytes(input: InputStream): ByteArray? {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    while (true) {
        val count = input.read(buffer)
        if (count < 0) break
        if (output.size() + count > MAX_PREVIEW_BYTES) return null
        output.write(buffer, 0, count)
    }
    return output.toByteArray().takeIf { it.isNotEmpty() }
}

private const val MAX_PREVIEW_BYTES = 8 * 1024 * 1024
private const val PREVIEW_TIMEOUT_MILLIS = 5_000
