package io.github.rwx.ui.model

data class ReplaySelectUiState(
    val revision: Long = 0,
    val replays: List<ReplayEntry> = emptyList(),
) {
    internal fun resolveAction(requestRevision: Long, action: ReplaySelectAction): ReplaySelectAction? = when (action) {
        ReplaySelectAction.Back -> action
        is ReplaySelectAction.SelectReplay -> if (requestRevision == revision) {
            replays.singleOrNull { it.replayName == action.replay.replayName && it.replayName.isNotBlank() }
                ?.takeIf { it == action.replay }?.let(ReplaySelectAction::SelectReplay)
        } else null
    }
}
