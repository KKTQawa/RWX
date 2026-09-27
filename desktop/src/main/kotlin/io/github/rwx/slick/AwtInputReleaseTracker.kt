package io.github.rwx.slick

/** EDT-only tracking for input whose release may arrive in a different window after focus moves. */
internal class AwtInputReleaseTracker(
    private val releaseKey: (Int, Char) -> Unit,
    private val releaseMouse: (Int, Int, Int) -> Unit,
) {
    private val keys = linkedMapOf<Int, Char>()
    private val buttons = linkedSetOf<Int>()
    private var mouseX = 0
    private var mouseY = 0

    fun keyPressed(key: Int, char: Char) { keys[key] = char }
    fun keyReleased(key: Int) { keys.remove(key) }
    fun mousePressed(button: Int, x: Int, y: Int) {
        buttons.add(button)
        mouseMoved(x, y)
    }
    fun mouseReleased(button: Int) { buttons.remove(button) }
    fun mouseMoved(x: Int, y: Int) { mouseX = x; mouseY = y }

    fun releaseAll() {
        val heldKeys = keys.toMap()
        val heldButtons = buttons.toList()
        keys.clear()
        buttons.clear()
        heldKeys.forEach { (key, char) -> releaseKey(key, char) }
        heldButtons.forEach { releaseMouse(it, mouseX, mouseY) }
    }
}
