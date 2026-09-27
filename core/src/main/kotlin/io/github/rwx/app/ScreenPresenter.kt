package io.github.rwx.app

import io.github.rwx.mod.registry.UiRegistry
import io.github.rwx.render.frame.GameFrame
import io.github.rwx.render.frame.GameViewport
import io.github.rwx.ui.AppScreen
import io.github.rwx.ui.AppScreenLayout
import io.github.rwx.ui.InGameOverlayState

internal class ScreenPresenter(
    private val bootstrap: AppBootstrap,
    private val viewport: () -> GameViewport,
) {
    var battleBackgroundVisible: Boolean = false
        private set
    private var inGameOverlay: InGameOverlayState? = null

    fun updateInGameOverlay(state: InGameOverlayState?, screen: AppScreen, lastExternalGameFrame: GameFrame?) {
        inGameOverlay = state
        if (screen == AppScreen.InGame) apply(screen, lastExternalGameFrame)
    }

    fun shouldShowRwMenuBackground(screen: AppScreen): Boolean =
        supportsMenuBattleBackground(screen) &&
                bootstrap.settingsModel.showMainMenuBackgroundDemo.value &&
                bootstrap.menuBackgroundSession.isMenuBackgroundActive() &&
                !bootstrap.gameSession.canResume()

    fun apply(
        screen: AppScreen,
        lastExternalGameFrame: GameFrame?,
    ) {
        val isMenuBackgroundPreparing = ensureMenuBackground(screen)
        val gameSession = bootstrap.gameSession
        val visibility = AppScreenLayout.visibilityFor(screen)
        val gameOverlay = inGameOverlay.takeIf { screen == AppScreen.InGame }
        val isRwMenuBackgroundVisible = shouldShowRwMenuBackground(screen)
        val isExternalModWindowOverlayVisible = screen == AppScreen.ModWindow && !gameSession.usesFrameCommandRendering
        val isExternalModHudOverlayVisible = gameOverlay?.let {
            it.visible && !gameSession.usesFrameCommandRendering
        } ?: shouldShowExternalModHudOverlay(
            hudVisible = visibility.hud,
            usesFrameCommandRendering = gameSession.usesFrameCommandRendering,
            hasActiveHudLayers = UiRegistry.hasActiveHudLayers(),
        )
        val isResumeBackgroundVisible = shouldShowResumeMenuBackground(screen, gameSession.canResume())
        val isExternalRwBackgroundVisible = shouldShowExternalRwBackgroundSurface(
            isRwMenuBackgroundVisible = isRwMenuBackgroundVisible,
            isResumeBackgroundVisible = isResumeBackgroundVisible,
            usesFrameCommandRendering = gameSession.usesFrameCommandRendering,
            usesNativeSurfaceForResumeBackground = gameSession.usesNativeSurfaceForResumeBackground,
        ) || (isMenuBackgroundPreparing && !gameSession.usesFrameCommandRendering)
        val isLastExternalFrameBackgroundVisible = supportsMenuBattleBackground(screen) &&
                !gameSession.usesFrameCommandRendering &&
                lastExternalGameFrame != null
        val isBattleBackgroundVisible = isRwMenuBackgroundVisible ||
                isResumeBackgroundVisible ||
                isLastExternalFrameBackgroundVisible

        battleBackgroundVisible = isBattleBackgroundVisible
        bootstrap.mainMenuSceneHost.setBattleBackgroundVisible(isBattleBackgroundVisible)

        bootstrap.settingsSceneHost.setActive(visibility.settings)
        gameSession.setGameVisible(
            shouldSetRwGameVisibleForScreen(screen) || isExternalRwBackgroundVisible,
            viewport(),
            uiOverlay = isExternalRwBackgroundVisible ||
                    isExternalModWindowOverlayVisible ||
                    isExternalModHudOverlayVisible,
            pausedBackground = shouldPauseRwGameForScreen(screen, isResumeBackgroundVisible) || gameOverlay?.pausesGame == true,
        )
    }

    private fun ensureMenuBackground(screen: AppScreen): Boolean {
        if (!bootstrap.settingsModel.showMainMenuBackgroundDemo.value) return false
        if (!supportsMenuBattleBackground(screen)) return false
        if (bootstrap.gameSession.canResume() || bootstrap.menuBackgroundSession.isMenuBackgroundActive()) return false
        bootstrap.menuBackgroundSession.prepareMenuBackgroundAsync(viewport())
        return true
    }
}

internal fun shouldShowExternalModHudOverlay(
    hudVisible: Boolean,
    usesFrameCommandRendering: Boolean,
    hasActiveHudLayers: Boolean,
): Boolean = hudVisible && !usesFrameCommandRendering && hasActiveHudLayers
