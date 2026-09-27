package io.github.rwx

import androidx.compose.ui.awt.ComposePanel
import io.github.rwx.slick.SlickUiFrame
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Cursor
import java.awt.Graphics
import java.awt.KeyboardFocusManager
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import javax.swing.JPanel
import javax.swing.SwingUtilities

/** Keeps Compose's Swing input/IME host, publishing only UI repaints for Slick to composite. */
internal class ComposeTexturePanel(private val panel: ComposePanel) : JPanel(BorderLayout()) {
    private var pointerOwner: Component? = null
    private var gameCursor: Cursor? = null
    @Volatile var uiFrame: SlickUiFrame? = null
        private set
    var nativePresentation = false
        set(value) {
            if (field == value) return
            field = value
            if (!value) {
                uiFrame = null
                pointerOwner?.cursor = gameCursor
                pointerOwner = null
                gameCursor = null
            } else SwingUtilities.invokeLater { if (nativePresentation) renderUi() }
            repaint()
        }

    init {
        isOpaque = false
        add(panel, BorderLayout.CENTER)
    }

    // Descendant invalidations reach us even though a heavyweight canvas covers this panel.
    override fun isPaintingOrigin() = true

    override fun paintImmediately(x: Int, y: Int, w: Int, h: Int) {
        if (nativePresentation) renderUi() else super.paintImmediately(x, y, w, h)
    }

    override fun paint(graphics: Graphics) {
        if (nativePresentation) renderUi() else super.paint(graphics)
    }

    private fun renderUi() {
        if (!isShowing || !panel.isVisible || width <= 0 || height <= 0) return
        val scale = graphicsConfiguration.defaultTransform
        val image = BufferedImage((width * scale.scaleX).toInt().coerceAtLeast(1),
            (height * scale.scaleY).toInt().coerceAtLeast(1), BufferedImage.TYPE_INT_ARGB_PRE)
        val graphics = image.createGraphics()
        try {
            graphics.scale(scale.scaleX, scale.scaleY)
            panel.paint(graphics)
        } finally { graphics.dispose() }
        uiFrame = SlickUiFrame(image.width, image.height, (image.raster.dataBuffer as DataBufferInt).data)
    }

    fun dispatchInput(event: InputEvent): Boolean {
        if (!nativePresentation || !panel.isShowing) return false
        when (event) {
            is MouseEvent -> {
                val point = SwingUtilities.convertPoint(event.component, event.point, panel)
                val pointerTarget = SwingUtilities.getDeepestComponentAt(panel, point.x, point.y) ?: panel
                val target = if (event is MouseWheelEvent) panel else pointerTarget
                target.dispatchEvent(SwingUtilities.convertMouseEvent(event.component, event, target))
                if (pointerOwner == null) {
                    pointerOwner = event.component
                    gameCursor = event.component.cursor
                }
                event.component.cursor = pointerTarget.cursor
            }
            is KeyEvent -> {
                val focus = KeyboardFocusManager.getCurrentKeyboardFocusManager().focusOwner
                val target = focus?.takeIf { SwingUtilities.isDescendingFrom(it, panel) }
                    ?: SwingUtilities.getDeepestComponentAt(panel, panel.width / 2, panel.height / 2) ?: panel
                KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(target,
                    KeyEvent(target, event.id, event.`when`, event.modifiersEx, event.keyCode, event.keyChar, event.keyLocation))
            }
        }
        event.consume()
        return true
    }
}
