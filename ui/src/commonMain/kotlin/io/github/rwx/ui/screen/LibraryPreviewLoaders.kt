package io.github.rwx.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import io.github.rwx.ui.theme.LocalColorScheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap

typealias ModPreviewLoader = suspend (String, String) -> ImageBitmap?
typealias ResourcePreviewLoader = suspend (String) -> ImageBitmap?

internal expect suspend fun readModPreviewBytes(path: String, entryName: String): ByteArray?
internal expect suspend fun readResourcePreviewBytes(url: String): ByteArray?

internal val defaultModPreviewLoader: ModPreviewLoader = { path, entryName ->
    withContext(Dispatchers.IO) { readModPreviewBytes(path, entryName)?.decodeToImageBitmap() }
}

internal val defaultResourcePreviewLoader: ResourcePreviewLoader = { url ->
    withContext(Dispatchers.IO) { readResourcePreviewBytes(url)?.decodeToImageBitmap() }
}

@Composable
internal fun LibraryPreviewImage(
    source: String?,
    revision: Long,
    imageTag: String,
    modifier: Modifier = Modifier,
    loader: suspend (String) -> ImageBitmap?,
) {
    val palette = LocalColorScheme.current.palette
    key(source, revision, loader) {
        val image by produceState<ImageBitmap?>(null) {
            value = if (source.isNullOrBlank()) null else try {
                loader(source)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                null
            }
        }
        Box(modifier.background(palette.surfaceBase), contentAlignment = Alignment.Center) {
            image?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().testTag("$imageTag:$source"),
                )
            }
        }
    }
}
