package io.github.rwx.ui.host

import io.github.rwx.session.GameLoadingStatus
import io.github.rwx.ui.model.LoadingUiState
import io.github.rwx.ui.model.SettingsModel

class LoadingSceneHost(
    private val model: SettingsModel = SettingsModel(),
) {
    private var uiState = LoadingUiState()
    private var warmupUiTextures: Boolean = false

    fun update(status: GameLoadingStatus) {
        uiState = LoadingUiState.from(status)
    }

    fun snapshot(): LoadingUiState = uiState

    fun showUiTextureWarmup() {
        warmupUiTextures = true
    }

    fun hideUiTextureWarmup() {
        warmupUiTextures = false
    }

    fun isUiTextureWarmupVisible(): Boolean = warmupUiTextures

    companion object {
        const val LOADING_SCENE_NAME: String = "loading-scene"
        const val GAME_LOADING_SCENE_NAME: String = "game-loading-scene"
        const val LOADING_LABEL: String = "Loading..."
    }
}
