package io.github.rwx.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import io.github.rwx.LegacyAssetBridge
import io.github.rwx.i18n.I18n
import io.github.rwx.ui.theme.LocalColorScheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap

typealias MapPreviewLoader = suspend (String) -> ImageBitmap?

/** Reuse platform asset resolution, but decode directly to Compose rather than allocating a Kool texture. */
internal val defaultMapPreviewLoader: MapPreviewLoader = { path ->
    withContext(Dispatchers.IO) {
        LegacyAssetBridge.openAsset(path)?.use { it.readBytes() }?.decodeToImageBitmap()
    }
}

@Composable
internal fun MapPreviewImage(
    path: String?,
    revision: Long,
    modifier: Modifier = Modifier,
    loader: MapPreviewLoader = defaultMapPreviewLoader,
) {
    val palette = LocalColorScheme.current.palette
    // A reload may reuse the same path with different bytes. Reset the composition (and cancel
    // its load) for either a new path or a new map-list revision; never reuse an old preview.
    key(path, revision, loader) {
        val image by produceState<ImageBitmap?>(null) {
            value = if (path == null) null else try {
                loader(path)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                null
            }
        }
        Box(modifier.background(palette.surfaceBase), contentAlignment = Alignment.Center) {
            val bitmap = image
            if (bitmap == null) {
                Text(I18n.common.noPreview(), color = palette.textDisabled, modifier = Modifier.testTag("preview-unavailable"))
            } else {
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().testTag("preview-image:$path"),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}
