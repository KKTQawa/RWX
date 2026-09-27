package io.github.rwx.ui.platform

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView

/**
 * Android menu-layer replacement host.
 *
 * Replacement (not overlay): the Activity never adds the Kool SurfaceView to the
 * view hierarchy for menus. This host owns a [ComposeView] added on top of the
 * native game view (Canvas/OpenGL presenter) and drives visibility from
 * [io.github.rwx.ui.AppUiState.showComposeOverlay].
 */
class AndroidComposeHost(
    private val composeView: ComposeView,
    private val onDispose: () -> Unit = {},
) : PlatformComposeHost {
    private var requestedVisible = false
    private var disposed = false
    override val isVisible: Boolean get() = composeView.visibility == View.VISIBLE

    override fun setContent(content: @Composable () -> Unit) {
        if (!disposed) composeView.setContent(content)
    }

    override fun setVisible(visible: Boolean) {
        if (disposed) return
        requestedVisible = visible
        val target = if (visible) View.VISIBLE else View.GONE
        if (composeView.visibility != target) {
            composeView.visibility = target
        }
        if (visible) {
            composeView.bringToFront()
            requestFocus()
        }
    }

    override fun requestFocus() {
        if (!disposed && composeView.visibility == View.VISIBLE) {
            composeView.requestFocus()
        }
    }

    override fun dispose() {
        if (disposed) return
        disposed = true
        try {
            composeView.visibility = View.GONE
            composeView.disposeComposition()
        } finally {
            onDispose()
        }
    }
}

fun createAndroidComposeHost(
    composeView: ComposeView,
    onDispose: () -> Unit = {},
): PlatformComposeHost = AndroidComposeHost(composeView, onDispose)

/**
 * Zero-arg factory required by the common expect. Android must use
 * [createAndroidComposeHost] with the Activity's ComposeView instead.
 */
actual fun createPlatformComposeHost(): PlatformComposeHost {
    throw UnsupportedOperationException(
        "Use createAndroidComposeHost(composeView) on Android"
    )
}
