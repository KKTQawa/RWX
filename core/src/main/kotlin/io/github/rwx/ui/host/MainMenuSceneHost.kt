package io.github.rwx.ui.host

import io.github.rwx.ui.model.MainMenuAction
import io.github.rwx.ui.model.MainMenuConditions
import io.github.rwx.ui.model.MainMenuItem
import io.github.rwx.ui.model.MainMenuViewModel
import io.github.rwx.ui.model.SettingsModel

class MainMenuSceneHost(
    private val model: SettingsModel = SettingsModel(),
    private val onAction: (MainMenuAction) -> Unit = {},
) {
    var items: List<MainMenuItem> = emptyList()
        private set

    private var battleBackgroundVisible: Boolean = false

    fun updateItems(conditions: MainMenuConditions) {
        val next = MainMenuViewModel.items(conditions)
        if (next != items) {
            items = next
        }
    }

    /** Snapshot for the Compose overlay; the returned list is immutable. */
    fun snapshotItems(): List<MainMenuItem> = items

    /** Dispatches a menu action to the handler. Kept separate so click routing is testable. */
    fun dispatch(action: MainMenuAction) {
        onAction(action)
    }

    fun setBattleBackgroundVisible(visible: Boolean) {
        battleBackgroundVisible = visible
    }

    fun isBattleBackgroundVisible(): Boolean = battleBackgroundVisible

    companion object {
        const val MAIN_MENU_SCENE_NAME: String = "main-menu"
        const val MENU_TITLE: String = "RWX"
        const val MENU_SUBTITLE: String = "Rusted Warfare Extension"
    }
}
