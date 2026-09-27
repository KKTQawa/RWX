package io.github.rwx.ui.model

import io.github.rwx.i18n.I18n
import io.github.rwx.session.GameLoadingStatus

/** Value snapshot of a full-screen load; it carries no engine objects or callbacks. */
data class LoadingUiState(
    val text: String = I18n.loading.loading(),
    val progress: Float? = null,
    val completedSteps: List<String> = emptyList(),
) {
    companion object {
        const val HISTORY_ROW_COUNT = 4

        fun from(status: GameLoadingStatus): LoadingUiState {
            val text = status.text.ifBlank { I18n.loading.loading() }
            return LoadingUiState(
                text = text,
                progress = status.progress,
                completedSteps = status.recentSteps.filter { it.isNotBlank() }
                    .dropLastWhile { it == text }.takeLast(HISTORY_ROW_COUNT),
            )
        }
    }
}
