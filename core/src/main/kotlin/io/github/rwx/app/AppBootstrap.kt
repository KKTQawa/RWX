package io.github.rwx.app

import io.github.rwx.AppMetadata
import io.github.rwx.PlatformBridge
import io.github.rwx.i18n.I18n
import io.github.rwx.mod.ModRepository
import io.github.rwx.net.ResourceBrowserRepository
import io.github.rwx.net.UpdateRepository
import io.github.rwx.session.GameSession
import io.github.rwx.settings.GameSettingsRepository
import io.github.rwx.ui.ColorSchemeRegistry
import io.github.rwx.ui.host.*
import io.github.rwx.ui.model.*
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform.getKoin

internal class ActionHandlers {
    var menu: (MainMenuAction) -> Unit = {}
    var pause: (PauseMenuAction) -> Unit = {}
    var levelSelect: (LevelSelectAction) -> Unit = {}
    var replaySelect: (ReplaySelectAction) -> Unit = {}
    var settings: (SettingsAction) -> Unit = {}
    var multiplayer: (MultiplayerAction) -> Unit = {}
    var mods: (ModsAction) -> Unit = {}
    var resourceBrowser: (ResourceBrowserAction) -> Unit = {}
    var battleRoom: (BattleRoomAction) -> Unit = {}
}

internal data class AppBootstrap(
    val platformBridge: PlatformBridge?,
    val appMetadata: AppMetadata,
    val gameSession: GameSession,
    val menuBackgroundSession: GameSession,
    val modRepository: ModRepository,
    val resourceBrowserRepository: ResourceBrowserRepository,
    val updateRepository: UpdateRepository,
    val settingsRepository: GameSettingsRepository,
    val actions: ActionHandlers,
    val settingsModel: SettingsModel,
    val loadingSceneHost: LoadingSceneHost,
    val mainMenuSceneHost: MainMenuSceneHost,
    val pauseSceneHost: PauseMenuSceneHost,
    val levelSelectSceneHost: LevelSelectSceneHost,
    val levelSelectViewModelFactory: LevelSelectViewModelFactory,
    val replaySelectSceneHost: ReplaySelectSceneHost,
    val settingsSceneHost: SettingsSceneHost,
    val multiplayerSceneHost: MultiplayerSceneHost,
    val modsSceneHost: ModsSceneHost,
    val resourceBrowserSceneHost: ResourceBrowserSceneHost,
    val loadingDialogSceneHost: LoadingDialogSceneHost,
    val battleRoomSceneHost: BattleRoomSceneHost,
    val dialogSceneHost: DialogSceneHost,
)

internal fun createAppBootstrap(
    options: AppOptions,
): AppBootstrap {
    val koin = getKoin()
    val platformBridge = runCatching { koin.get<PlatformBridge>() }.getOrNull()
    val appMetadata = platformBridge?.appMetadata ?: runCatching { koin.get<AppMetadata>() }.getOrDefault(AppMetadata())

    val gameSession = koin.get<GameSession>()
    val menuBackgroundSession = gameSession
    val modRepository = koin.get<ModRepository>()
    val resourceBrowserRepository = koin.get<ResourceBrowserRepository>()
    val updateRepository = koin.get<UpdateRepository>()
    val settingsRepository = koin.get<GameSettingsRepository>()
    val actions = ActionHandlers()

    val settingsModel = SettingsModel()
    settingsRepository.loadInto(settingsModel)
    if (options.colorSchemeId != ColorSchemeRegistry.defaultSchemeId) {
        settingsModel.selectedColorSchemeId.value = options.colorSchemeId
    }

    val loadingDialogSceneHost = koin.get<LoadingDialogSceneHost> {
        parametersOf(settingsModel)
    }

    val loadingSceneHost = LoadingSceneHost(settingsModel)

    val mainMenuSceneHost = koin.get<MainMenuSceneHost> {
        parametersOf(settingsModel, { action: MainMenuAction -> actions.menu(action) })
    }
    mainMenuSceneHost.updateItems(MainMenuConditions(isDesktop = options.isDesktop))

    val pauseSceneHost = koin.get<PauseMenuSceneHost> {
        parametersOf(settingsModel, { action: PauseMenuAction -> actions.pause(action) })
    }
    pauseSceneHost.updateItems(PauseMenuConditions(canSave = true))

    val levelSelectSceneHost = koin.get<LevelSelectSceneHost> {
        parametersOf(
            settingsModel,
            LevelSelectActionHandler { actions.levelSelect(it) },
        )
    }
    val levelSelectViewModelFactory = koin.get<LevelSelectViewModelFactory>()

    val replaySelectSceneHost = koin.get<ReplaySelectSceneHost> {
        parametersOf(
            settingsModel,
            ReplaySelectActionHandler { actions.replaySelect(it) },
        )
    }

    val settingsSceneHost = koin.get<SettingsSceneHost> {
        parametersOf(settingsModel, { action: SettingsAction -> actions.settings(action) })
    }

    val multiplayerSceneHost = koin.get<MultiplayerSceneHost> {
        parametersOf(settingsModel, { action: MultiplayerAction -> actions.multiplayer(action) })
    }

    val modsSceneHost = koin.get<ModsSceneHost> {
        parametersOf(settingsModel, { action: ModsAction -> actions.mods(action) })
    }
    modsSceneHost.updateMods(modRepository.listMods())

    val resourceBrowserSceneHost = koin.get<ResourceBrowserSceneHost> {
        parametersOf(settingsModel, { action: ResourceBrowserAction -> actions.resourceBrowser(action) })
    }

    val battleRoomSceneHost = koin.get<BattleRoomSceneHost> {
        parametersOf(settingsModel, { action: BattleRoomAction -> actions.battleRoom(action) })
    }

    val dialogSceneHost = koin.get<DialogSceneHost> {
        parametersOf(settingsModel)
    }

    options.levelSelectMode?.let(levelSelectSceneHost::updateMaps)
    options.settingsPage?.let(settingsSceneHost::showPage)
    if (options.showDemoDialog) {
        dialogSceneHost.show(
            Dialog(
                title = "Connection Error",
                message = "Unable to reach the server.\nPlease check your connection and try again.",
                buttons = listOf(DialogButton(I18n.common.ok())),
            ),
        )
    }

    return AppBootstrap(
        platformBridge = platformBridge,
        appMetadata = appMetadata,
        gameSession = gameSession,
        menuBackgroundSession = menuBackgroundSession,
        modRepository = modRepository,
        resourceBrowserRepository = resourceBrowserRepository,
        updateRepository = updateRepository,
        settingsRepository = settingsRepository,
        actions = actions,
        settingsModel = settingsModel,
        loadingSceneHost = loadingSceneHost,
        mainMenuSceneHost = mainMenuSceneHost,
        pauseSceneHost = pauseSceneHost,
        levelSelectSceneHost = levelSelectSceneHost,
        levelSelectViewModelFactory = levelSelectViewModelFactory,
        replaySelectSceneHost = replaySelectSceneHost,
        settingsSceneHost = settingsSceneHost,
        multiplayerSceneHost = multiplayerSceneHost,
        modsSceneHost = modsSceneHost,
        resourceBrowserSceneHost = resourceBrowserSceneHost,
        loadingDialogSceneHost = loadingDialogSceneHost,
        battleRoomSceneHost = battleRoomSceneHost,
        dialogSceneHost = dialogSceneHost,
    )
}
