package io.github.rwx.ui.platform

import androidx.compose.runtime.Composable

/**
 * Platform-specific host for Compose UI lifecycle.
 * Desktop: manages ComposePanel in overlayWindow
 * Android: manages ComposeView in Activity
 */
interface PlatformComposeHost {
    val isVisible: Boolean

    /**
     * Set the root Compose content.
     */
    fun setContent(content: @Composable () -> Unit)

    /**
     * Show/hide the Compose UI overlay.
     */
    fun setVisible(visible: Boolean)

    /**
     * Request focus for text input.
     */
    fun requestFocus()

    /**
     * Dispose resources when closing.
     */
    fun dispose()
}
