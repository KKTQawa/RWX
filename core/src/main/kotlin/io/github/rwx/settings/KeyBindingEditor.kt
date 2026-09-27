package io.github.rwx.settings

import com.corrodinggames.rts.gameFramework.KeyBinding
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes.AndroidCodes
import io.github.rwx.ui.model.*
import com.corrodinggames.rts.gameFramework.InputController as LegacyInputController

/** Shared frontend-only editing logic.*/
class KeyBindingEditor(
    private val controllerProvider: () -> LegacyInputController,
    private val persist: (LegacyInputController) -> Unit,
    private val onChanged: () -> Unit = {},
) {
    private data class ActiveCapture(
        val request: KeyBindingCapture,
        val controller: LegacyInputController,
        val binding: KeyBinding,
    )

    private var enabled = false
    private var active: ActiveCapture? = null
    private var lastCaptureRequestId = 0L

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        if (!enabled) cancelCapture()
    }

    fun beginCapture(request: KeyBindingCapture): Boolean {
        if (request.requestId <= lastCaptureRequestId) return false
        lastCaptureRequestId = request.requestId
        active = null
        if (enabled) {
            val controller = controllerProvider()
            editableBinding(controller, request.target)?.let { active = ActiveCapture(request, controller, it) }
        }
        onChanged()
        return active != null
    }

    fun cancelCapture(requestId: Long? = null): Boolean {
        val current = active ?: return false
        if (requestId != null && current.request.requestId != requestId) return false
        active = null
        onChanged()
        return true
    }

    fun activeCapture(): KeyBindingCapture? = validCapture()?.request

    fun captureKey(requestId: Long, keyCode: Int, modifiers: Int): Boolean {
        val current = validCapture() ?: return false
        if (current.request.requestId != requestId) return false
        if (keyCode == AndroidCodes.KEYCODE_ESCAPE) return cancelCapture(requestId)
        if (!KeyBindingInput.isBindable(keyCode) || modifiers and KeyBindingInput.MODIFIERS != modifiers) return false
        current.binding.a(keyCode, current.request.target.slot, modifiers, true)
        active = null
        onChanged()
        persist(current.controller)
        return true
    }

    fun clear(target: KeyBindingTarget): Boolean {
        if (!enabled) return false
        val controller = controllerProvider()
        val binding = editableBinding(controller, target) ?: return false
        binding.a(0, target.slot, 0, true)
        active = null
        onChanged()
        persist(controller)
        return true
    }

    fun legacyRows(): List<SettingKeyBindingRow> {
        val capture = validCapture()?.request
        val controller = controllerProvider()
        return controller.al.mapIndexedNotNull { index, binding ->
            if (!binding.isDefault) return@mapIndexedNotNull null
            SettingKeyBindingRow(
                index = index,
                keyBinding = binding,
                primaryText = binding.b(0),
                secondaryText = binding.b(1),
                primaryOverlaps = hasOverlap(controller, binding, 0),
                secondaryOverlaps = hasOverlap(controller, binding, 1),
                activeSlot = capture?.target?.takeIf { it.index == index }?.slot,
            )
        }
    }

    fun snapshot(): KeyBindingsState {
        val rows = legacyRows().map { row ->
            KeyBindingRowState(
                row.index, row.keyBinding.e(), row.keyBinding.name, row.keyBinding.d(),
                row.primaryText, row.secondaryText, row.primaryOverlaps, row.secondaryOverlaps,
            )
        }
        return KeyBindingsState(rows, active?.request, lastCaptureRequestId)
    }

    private fun validCapture(): ActiveCapture? {
        val current = active ?: return null
        if (!enabled || controllerProvider() !== current.controller ||
            editableBinding(current.controller, current.request.target) !== current.binding
        ) {
            cancelCapture()
            return null
        }
        return current
    }

    private fun editableBinding(controller: LegacyInputController, target: KeyBindingTarget): KeyBinding? {
        if (target.slot !in 0..1 || target.index < 0) return null
        return controller.al.getOrNull(target.index)?.takeIf {
            it.isDefault && !it.d() && it.e() == target.bindingId
        }
    }

    private fun hasOverlap(controller: LegacyInputController, binding: KeyBinding, slot: Int): Boolean {
        val input = binding.a(slot) ?: return false
        if (input.d()) return false
        return controller.al.any { other ->
            other !== binding && other.bindings.any { !it.d() && input.a(it) }
        }
    }
}

object KeyBindingInput {
    const val CTRL = 1
    const val SHIFT = 2
    const val ALT = 4
    const val MODIFIERS = CTRL or SHIFT or ALT

    private val specialKeys = setOf(
        AndroidCodes.KEYCODE_DPAD_UP, AndroidCodes.KEYCODE_DPAD_DOWN,
        AndroidCodes.KEYCODE_DPAD_LEFT, AndroidCodes.KEYCODE_DPAD_RIGHT,
        AndroidCodes.KEYCODE_TAB, AndroidCodes.KEYCODE_SPACE, AndroidCodes.KEYCODE_ENTER,
        AndroidCodes.KEYCODE_DEL, AndroidCodes.KEYCODE_FORWARD_DEL, AndroidCodes.KEYCODE_INSERT,
        AndroidCodes.KEYCODE_MOVE_HOME, AndroidCodes.KEYCODE_MOVE_END, AndroidCodes.KEYCODE_BREAK,
        AndroidCodes.KEYCODE_PAGE_UP, AndroidCodes.KEYCODE_PAGE_DOWN,
        AndroidCodes.KEYCODE_COMMA, AndroidCodes.KEYCODE_PERIOD, AndroidCodes.KEYCODE_MINUS,
        AndroidCodes.KEYCODE_EQUALS, AndroidCodes.KEYCODE_LEFT_BRACKET, AndroidCodes.KEYCODE_RIGHT_BRACKET,
        AndroidCodes.KEYCODE_BACKSLASH, AndroidCodes.KEYCODE_SEMICOLON, AndroidCodes.KEYCODE_APOSTROPHE,
        AndroidCodes.KEYCODE_SLASH, AndroidCodes.KEYCODE_GRAVE, AndroidCodes.KEYCODE_PLUS,
    )

    fun isBindable(code: Int): Boolean = code in AndroidCodes.KEYCODE_A..AndroidCodes.KEYCODE_Z ||
        code in AndroidCodes.KEYCODE_0..AndroidCodes.KEYCODE_9 ||
        code in AndroidCodes.KEYCODE_F1..AndroidCodes.KEYCODE_F12 ||
        code in AndroidCodes.KEYCODE_NUMPAD_0..AndroidCodes.KEYCODE_NUMPAD_EQUALS || code in specialKeys
}
