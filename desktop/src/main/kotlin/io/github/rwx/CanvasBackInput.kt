package io.github.rwx

import java.awt.Canvas
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.KeyEvent
import javax.swing.SwingUtilities
import io.github.rwx.slick.SlickCanvasHost


internal class CanvasBackInput(
    private val canvas: Canvas,
    private val canHandleBack: () -> Boolean = { true },
    private val onBack: () -> Unit,
) : KeyEventDispatcher, AutoCloseable {
    private var pressed = false
    private var closed = false
    private val focusListener = object : FocusAdapter() {
        override fun focusLost(event: FocusEvent) { pressed = false }
    }
    init {
        check(SwingUtilities.isEventDispatchThread())
        canvas.addFocusListener(focusListener)
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (closed || event.component !== canvas) return false
        if (SlickCanvasHost.dispatchOverlayInput(event)) return true
        if (event.keyCode != KeyEvent.VK_ESCAPE) return false
        if (!canHandleBack()) return false
        when (event.id) {
            KeyEvent.KEY_PRESSED -> pressed = true
            KeyEvent.KEY_RELEASED -> if (pressed) { pressed = false; onBack() }
        }
        event.consume()
        return true
    }

    override fun close() {
        check(SwingUtilities.isEventDispatchThread())
        if (closed) return
        closed = true
        pressed = false
        canvas.removeFocusListener(focusListener)
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this)
    }
}
