package io.github.rwx.ui.host

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import io.github.rwx.ui.model.*

class DialogSceneHost(
    @Suppress("unused") private val model: SettingsModel = SettingsModel(),
    private val onVisibilityChanged: (Boolean) -> Unit = {},
) {
    private val resultScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val choiceLock = Any()
    private val pendingChoices = mutableSetOf<DialogInputChoice>()
    @Volatile
    private var closed = false
    private var composeOwned = false
    private val store = DialogStateStore(onChanged = ::syncPresentation)

    /** Logical visibility is independent of which renderer owns the modal. */
    val isVisible: Boolean get() = !closed && store.isVisible
    fun snapshot(): DialogUiState? = if (closed) null else store.snapshot()
    fun refreshValidity() { if (!closed) store.refreshValidity() }

    fun show(dialog: Dialog, isValid: () -> Boolean = { true }) {
        if (closed) return
        val input = dialog.textInput
        val choose = input?.onChooseInput
        val ownedDialog = if (choose == null) dialog else dialog.copy(
            textInput = input.copy(onChooseInput = { complete ->
                choose { choice -> postChoice(choice, complete) }
            }),
        )
        store.show(ownedDialog, isValid)
    }

    fun hide() { if (!closed) store.hide() }
    fun hide(revision: Long) { if (!closed) store.hide(revision) }
    fun dispatch(revision: Long, action: DialogUiAction): Boolean = !closed && store.dispatch(revision, action)

    /** Terminal, frontend-confined close. Chooser callbacks may still arrive on other threads. */
    fun close() {
        val pending = synchronized(choiceLock) {
            if (closed) return
            closed = true
            pendingChoices.toList().also { pendingChoices.clear() }
        }
        resultScope.cancel()
        try {
            store.hide()
        } finally {
            // A cancelled Main task may never run. Its resource cleanup cannot live only in its body.
            pending.forEach { it.dispose() }
        }
    }

    private fun postChoice(choice: DialogInputChoice?, complete: (DialogInputChoice?) -> Unit) {
        val accepted = synchronized(choiceLock) {
            if (closed) false else {
                choice?.let(pendingChoices::add)
                true
            }
        }
        if (!accepted) {
            choice?.dispose()
            return
        }
        resultScope.launch {
            val deliver = synchronized(choiceLock) {
                pendingChoices.remove(choice)
                !closed
            }
            if (deliver) complete(choice) else choice?.dispose()
        }.invokeOnCompletion { error ->
            if (error != null) {
                synchronized(choiceLock) { pendingChoices.remove(choice) }
                choice?.dispose()
            }
        }
    }

    fun setComposeOwned(owned: Boolean) {
        if (closed || composeOwned == owned) return
        composeOwned = owned
        notifyVisibility()
    }

    private fun syncPresentation() {
        notifyVisibility()
    }

    private fun notifyVisibility(notifyVisibility: Boolean = true) {
        if (notifyVisibility) onVisibilityChanged(isVisible && !composeOwned)
    }

    companion object {
        const val ERROR_DIALOG_SCENE_NAME: String = "dialog"
    }
}
