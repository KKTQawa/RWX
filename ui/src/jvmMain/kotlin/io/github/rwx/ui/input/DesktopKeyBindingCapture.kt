package io.github.rwx.ui.input

import androidx.compose.ui.input.key.*
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes.AndroidCodes
import io.github.rwx.ui.model.KeyBindingCapture
import io.github.rwx.ui.model.SettingsUiAction

/** Tracks key-downs even outside capture, to reject repeats and swallow matching releases. */
internal class DesktopKeyBindingCapture {
    private val pressed = mutableSetOf<Key>()
    private val swallowed = mutableSetOf<Key>()
    private var finishedRequest: Long? = null

    fun finish(requestId: Long?) {
        if (requestId != null) finishedRequest = requestId
    }

    fun reset() {
        pressed.clear()
        swallowed.clear()
        finishedRequest = null
    }

    fun handle(event: KeyEvent, capture: KeyBindingCapture?, onAction: (SettingsUiAction) -> Unit): Boolean {
        val key = event.key
        when (event.type) {
            KeyEventType.KeyUp -> {
                pressed.remove(key)
                return swallowed.remove(key) || capture != null
            }
            KeyEventType.KeyDown -> {
                val repeated = !pressed.add(key)
                if (capture == null && key !in swallowed) return false
                swallowed += key
                if (capture != null && !repeated && capture.requestId != finishedRequest) {
                    DesktopKeyCodeMapping.stroke(event)?.let { stroke ->
                        finish(capture.requestId)
                        if (stroke.keyCode == AndroidCodes.KEYCODE_ESCAPE) {
                            onAction(SettingsUiAction.CancelKeyCapture(capture.requestId))
                        } else {
                            onAction(SettingsUiAction.CaptureKey(capture.requestId, stroke.keyCode, stroke.modifiers))
                        }
                    }
                }
                return true
            }
            else -> return capture != null || swallowed.isNotEmpty()
        }
    }
}
