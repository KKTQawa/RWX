package io.github.rwx.ui.host

import io.github.rwx.ui.model.PauseMenuAction
import io.github.rwx.ui.model.PauseMenuConditions
import io.github.rwx.ui.model.PauseMenuItem
import io.github.rwx.ui.model.PauseMenuViewModel
import io.github.rwx.ui.model.SettingsModel

/**
 * Holds pause-menu state for the Compose overlay: a button list (Resume, Save?, Settings,
 * Surrender, Exit Game) matching the classic in-game menu layout from menus.ingame.* keys.
 * Rendering is owned by Compose; this host only publishes snapshots and routes actions.
 */
class PauseMenuSceneHost(
    private val model: SettingsModel = SettingsModel(),
    private val onAction: (PauseMenuAction) -> Unit = {},
) {
    var items: List<PauseMenuItem> = emptyList()
        private set

    private var conditions = PauseMenuConditions()

    fun snapshotItems(): List<PauseMenuItem> = PauseMenuViewModel.items(conditions)

    fun updateItems(conditions: PauseMenuConditions) {
        this.conditions = conditions
        val next = PauseMenuViewModel.items(conditions)
        if (next != items) {
            items = next
        }
    }

    fun dispatch(action: PauseMenuAction) = onAction(action)

    companion object {
        const val PAUSE_SCENE_NAME: String = "pause"
    }
}
