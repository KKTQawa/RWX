package io.github.rwx.ui.model

/**
 * Input an overlay renderer forwards to the running game because no overlay control consumed it.
 * Positions are the game canvas' logical pixels, the same space as the game's own AWT input.
 */
sealed interface GameInputEvent {
    /** Ownership ended (modal, focus loss, hidden/disposed window); safe even after forwarding stops. */
    data object ReleaseAll : GameInputEvent

    data class PointerMove(val x: Float, val y: Float) : GameInputEvent

    data class PointerButton(
        val x: Float,
        val y: Float,
        val button: GamePointerButton,
        val isDown: Boolean,
    ) : GameInputEvent

    /** Wheel movement in notches; positive scrolls up, matching the legacy overlay sink. */
    data class Scroll(val notches: Float) : GameInputEvent

    data class Key(val androidKeyCode: Int, val isDown: Boolean) : GameInputEvent
}

/** [legacyId] is the pointer id the engine's overlay input path expects. */
enum class GamePointerButton(val legacyId: Int) {
    Left(1),
    Right(2),
    Middle(3),
}
