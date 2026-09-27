package io.github.rwx.ui.host

import io.github.rwx.ui.model.*

class ReplaySelectSceneHost(
    private val model: SettingsModel = SettingsModel(),
    private val viewModelFactory: ReplaySelectViewModelFactory,
    private val onAction: ReplaySelectActionHandler = ReplaySelectActionHandler {},
) {
    private val replays = mutableListOf<ReplayEntry>()
    private var revision = 0L

    fun refresh() {
        updateReplays(viewModelFactory.create().items())
    }

    fun updateReplays(entries: List<ReplayEntry>) {
        revision++
        synchronized(replays) {
            replays.clear()
            replays.addAll(entries)
        }
    }

    fun snapshot(): ReplaySelectUiState =
        ReplaySelectUiState(revision, synchronized(replays) { replays.toList() })

    fun dispatch(action: ReplaySelectAction) = onAction.onAction(action)

    companion object {
        const val REPLAY_SELECT_SCENE_NAME: String = "replay-select"
    }
}
