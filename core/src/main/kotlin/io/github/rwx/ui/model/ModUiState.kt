package io.github.rwx.ui.model

import androidx.compose.runtime.Composable
import io.github.rwx.mod.api.ModWindowContext

/** One mod HUD layer; [content] is the mod's Compose lambda and is composed as-is. */
data class ModHudLayerUiState(
    val id: String,
    val order: Int,
    val content: @Composable () -> Unit,
)

/**
 * The HUD layers currently registered by mods, ordered for composition. [revision] changes with
 * every registration change so a renderer can tell a rebuilt list from the previous one.
 */
data class ModHudUiState(
    val revision: Long = 0,
    val layers: List<ModHudLayerUiState> = emptyList(),
) {
    companion object {
        fun of(revision: Long, layers: Collection<ModHudLayerUiState>): ModHudUiState =
            ModHudUiState(revision, layers.sortedWith(compareBy<ModHudLayerUiState> { it.order }.thenBy { it.id }))
    }
}

/**
 * The mod window screen. [content] and [context] are null when no window is active; [revision]
 * changes when a window is opened, refreshed, or closed, so content is rebuilt from scratch on
 * every explicit refresh exactly like the previous renderer did.
 */
data class ModWindowUiState(
    val revision: Long = 0,
    val title: String = "",
    val content: (@Composable (ModWindowContext) -> Unit)? = null,
    val context: ModWindowContext? = null,
) {
    val hasWindow: Boolean get() = content != null && context != null
}

sealed interface ModWindowAction {
    /** Returns to the game; matches the window's own [ModWindowContext.close]. */
    data object Close : ModWindowAction
}
