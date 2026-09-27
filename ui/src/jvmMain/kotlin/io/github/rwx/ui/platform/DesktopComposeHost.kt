package io.github.rwx.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.awt.RenderSettings
import java.awt.Color
import java.awt.Component
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JLayeredPane
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.SwingUtilities

@OptIn(ExperimentalComposeUiApi::class)
class DesktopComposeHost(
    private val owner: JFrame,
    private val fallbackFocus: Component,
    val composePanel: ComposePanel = createDesktopComposePanel(),
    private val onVisibilityChanged: (Boolean) -> Unit = { composePanel.isVisible = it },
    private val onDispose: () -> Unit = {},
) : PlatformComposeHost {
    init { checkEdt() }
    private var disposed = false
    override val isVisible: Boolean get() = !disposed && composePanel.isVisible
    private val ownerListener = object : WindowAdapter() {
        override fun windowClosed(e: WindowEvent) = dispose()
    }

    private val managedLayout = composePanel.parent == null
    private val resizeListener = object : ComponentAdapter() {
        override fun componentResized(e: ComponentEvent) = syncBounds()
    }
    private fun syncBounds() {
        if (!disposed && managedLayout) {
            composePanel.bounds = SwingUtilities.convertRectangle(owner.contentPane, owner.contentPane.bounds.apply { x = 0; y = 0 }, owner.layeredPane)
        }
    }

    init {
        owner.addWindowListener(ownerListener)
        if (managedLayout) {
            owner.layeredPane.add(composePanel, JLayeredPane.PALETTE_LAYER, 0)
            owner.contentPane.addComponentListener(resizeListener)
            syncBounds()
        }
    }

    override fun setContent(content: @Composable () -> Unit) {
        checkEdt()
        if (!disposed) composePanel.setContent(content)
    }

    override fun setVisible(visible: Boolean) {
        checkEdt()
        if (disposed || composePanel.isVisible == visible) return
        val focus = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().focusOwner
        val hadFocus = focus != null && (focus === composePanel || SwingUtilities.isDescendingFrom(focus, composePanel))
        onVisibilityChanged(visible)
        if (visible) requestFocus() else if (hadFocus && fallbackFocus.isShowing) fallbackFocus.requestFocusInWindow()
    }

    override fun requestFocus() {
        checkEdt()
        if (isVisible) composePanel.requestFocusInWindow()
    }

    override fun dispose() {
        checkEdt()
        if (disposed) return
        disposed = true
        owner.removeWindowListener(ownerListener)
        owner.contentPane.removeComponentListener(resizeListener)
        composePanel.isVisible = false
        composePanel.dispose()
        composePanel.parent?.remove(composePanel)
        onDispose()
    }

    private fun checkEdt() {
        check(SwingUtilities.isEventDispatchThread()) { "Compose host operations must run on the EDT" }
    }
}

/** SwingGraphics keeps Compose popups/dialog layers in the same Swing hierarchy. */
@OptIn(ExperimentalComposeUiApi::class)
fun createDesktopComposePanel(): ComposePanel {
    check(SwingUtilities.isEventDispatchThread()) { "Create Compose panels on the EDT" }
    return ComposePanel(renderSettings = RenderSettings.SwingGraphics()).apply {
        name = "rwx-compose-ui"
        isOpaque = false
        background = Color(0, 0, 0, 0)
    }
}

actual fun createPlatformComposeHost(): PlatformComposeHost {
    throw UnsupportedOperationException("Desktop Compose requires the application's JFrame and in-frame panel")
}

fun createDesktopComposeHost(
    owner: JFrame,
    fallbackFocus: Component,
    composePanel: ComposePanel,
    onVisibilityChanged: (Boolean) -> Unit = { composePanel.isVisible = it },
    onDispose: () -> Unit = {},
): PlatformComposeHost = DesktopComposeHost(owner, fallbackFocus, composePanel, onVisibilityChanged, onDispose)
