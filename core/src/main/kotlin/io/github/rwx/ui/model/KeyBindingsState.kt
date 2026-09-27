package io.github.rwx.ui.model

import java.util.concurrent.atomic.AtomicLong

/** Indices are checked together with the persistent binding id; headers/debug bindings are not editable. */
data class KeyBindingTarget(val index: Int, val bindingId: String, val slot: Int)

/** Allocated before dispatch so a fast first key can be queued behind BeginCapture on the frontend. */
data class KeyBindingCapture(
    val target: KeyBindingTarget,
    val requestId: Long = nextRequestId.incrementAndGet(),
) {
    companion object {
        private val nextRequestId = AtomicLong()
    }
}

data class KeyBindingRowState(
    val index: Int,
    val bindingId: String,
    val name: String,
    val isSection: Boolean,
    val primaryText: String,
    val secondaryText: String,
    val primaryOverlaps: Boolean,
    val secondaryOverlaps: Boolean,
) {
    fun target(slot: Int): KeyBindingTarget = KeyBindingTarget(index, bindingId, slot)
}

data class KeyBindingsState(
    val rows: List<KeyBindingRowState>,
    val activeCapture: KeyBindingCapture? = null,
    val lastCaptureRequestId: Long = 0,
)
