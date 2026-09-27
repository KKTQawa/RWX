package io.github.rwx.ui.model

/** A frontend-owned map-load result copied for UI threads; never exposes Kool's mutable list. */
data class LevelSelectUiState(
    val revision: Long = 0,
    val currentMode: LevelSelectMode = LevelSelectMode.Skirmish,
    val title: String = currentMode.label,
    val availableModes: List<LevelSelectMode> = LevelSelectMode.entries,
    val maps: List<MapEntry> = emptyList(),
    val isLoading: Boolean = false,
    val loadError: String? = null,
) {
    /** Resolve selection against the current result, not a map captured before a mode switch/reload. */
    internal fun resolveAction(requestRevision: Long, action: LevelSelectAction): LevelSelectAction? = when (action) {
        is LevelSelectAction.SelectMap -> if (requestRevision == revision && !isLoading && loadError == null) {
            maps.firstOrNull { it == action.map }?.let { LevelSelectAction.SelectMap(it) }
        } else null
        // Mode switches and Back remain available during loading. In particular, a newer mode
        // choice must not be dropped just because a previous choice already started loading.
        is LevelSelectAction.SelectMode -> action.takeIf { it.mode in availableModes }
        LevelSelectAction.Back -> action
    }
}
