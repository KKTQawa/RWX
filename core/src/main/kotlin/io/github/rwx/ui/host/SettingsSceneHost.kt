package io.github.rwx.ui.host

import com.corrodinggames.rts.gameFramework.GameEngine
import com.corrodinggames.rts.gameFramework.SettingsEngine
import io.github.rwx.settings.KeyBindingEditor
import io.github.rwx.ui.model.*
import com.corrodinggames.rts.gameFramework.InputController as LegacyInputController

class SettingsSceneHost(
    val model: SettingsModel = SettingsModel(),
    private val onAction: (SettingsAction) -> Unit = {},
) {
    private val viewModel = SettingsViewModel(model)
    private var currentPageIndex: Int = 0
    private var active = false
    private val fallbackInputController: LegacyInputController by lazy {
        LegacyInputController().also { controller ->
            controller.a()
            SettingsEngine.getInstance().loadKeyBindingsInto(controller)
        }
    }
    val keyBindingEditor = KeyBindingEditor(
        controllerProvider = { GameEngine.getInstance()?.inputController ?: fallbackInputController },
        persist = { controller ->
            SettingsEngine.getInstance().saveKeyBindingsFromInputController(controller)
            dispatch(SettingsAction.ApplyChanges)
        },
    )

    fun pageContent(): SettingsPageContent = viewModel.pageAt(currentPageIndex)

    fun snapshot(): SettingsPageState {
        val page = pageContent().snapshot()
        return if (page.page == SettingsPage.KeyBindings) page.copy(keyBindings = keyBindingEditor.snapshot()) else page
    }

    fun setActive(active: Boolean) {
        this.active = active
        keyBindingEditor.setEnabled(active && currentPageIndex == SettingsPage.KeyBindings.ordinal &&
            SettingsPage.KeyBindings in visibleSettingsPages())
    }

    fun dispatch(action: SettingsAction) {
        if (action == SettingsAction.Back) keyBindingEditor.cancelCapture()
        onAction(action)
    }

    fun showPage(page: SettingsPage) {
        if (currentPageIndex != page.ordinal) keyBindingEditor.cancelCapture()
        currentPageIndex = page.ordinal
        setActive(active)
    }

}
