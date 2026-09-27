package io.github.rwx.ui.input

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.nativeKeyLocation
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import io.github.rwx.ui.model.GameInputEvent
import io.github.rwx.ui.model.GamePointerButton

/**
 * Forwards pointer and key input the mod HUD did not consume to the running game.
 *
 * Pointer events are observed in the final pass, after HUD controls had their chance to consume
 * them; positions are converted from Compose pixels to the canvas' logical pixels, the space the
 * game's own AWT input uses. Keys reach [onGameInput] only when no focused HUD control handled them.
 */
@OptIn(ExperimentalComposeUiApi::class)
internal fun Modifier.forwardUnconsumedInputToGame(onGameInput: (GameInputEvent) -> Unit): Modifier = this
    .pointerInput(onGameInput) {
        val scale = density
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Final)
                val change = event.changes.lastOrNull() ?: continue
                if (change.isConsumed) continue
                val x = change.position.x / scale
                val y = change.position.y / scale
                when (event.type) {
                    PointerEventType.Move, PointerEventType.Enter -> onGameInput(GameInputEvent.PointerMove(x, y))
                    PointerEventType.Press, PointerEventType.Release -> event.button?.toGameButton()?.let { button ->
                        onGameInput(GameInputEvent.PointerButton(x, y, button, isDown = event.type == PointerEventType.Press))
                    }
                    // Compose reports wheel-down as a positive delta; the game counts notches upward.
                    PointerEventType.Scroll -> change.scrollDelta.y.takeIf { it != 0f }?.let { onGameInput(GameInputEvent.Scroll(-it)) }
                    else -> Unit
                }
            }
        }
    }
    .onKeyEvent { event ->
        val isDown = when (event.type) {
            KeyEventType.KeyDown -> true
            KeyEventType.KeyUp -> false
            else -> return@onKeyEvent false
        }
        val code = DesktopKeyCodeMapping.gameKeyCode(event) ?: return@onKeyEvent false
        onGameInput(GameInputEvent.Key(code, isDown))
        true
    }

private fun PointerButton.toGameButton(): GamePointerButton? = when (this) {
    PointerButton.Primary -> GamePointerButton.Left
    PointerButton.Secondary -> GamePointerButton.Right
    PointerButton.Tertiary -> GamePointerButton.Middle
    else -> null
}

/** Game hotkeys include the modifier keys themselves, which key bindings deliberately ignore. */
internal fun DesktopKeyCodeMapping.gameKeyCode(event: KeyEvent): Int? =
    gameKeyCode(event.key.nativeKeyCode, event.key.nativeKeyLocation)
