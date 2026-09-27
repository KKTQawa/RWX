package io.github.rwx

import com.corrodinggames.rts.gameFramework.GameEngine
import com.corrodinggames.rts.gameFramework.InputController
import com.corrodinggames.rts.gameFramework.SettingsEngine
import io.github.rwx.app.AppOptions
import io.github.rwx.app.installApp
import io.github.rwx.di.coreModule
import io.github.rwx.di.desktopModule
import io.github.rwx.i18n.LocaleSettings
import io.github.rwx.render.canvas.GameFontMetrics
import io.github.rwx.settings.GameSettingsRepository
import io.github.rwx.slick.SlickFramePresenter
import io.github.rwx.slick.SlickGameSession
import io.github.rwx.ui.AppUiState
import io.github.rwx.ui.ColorSchemeRegistry
import io.github.rwx.ui.host.LoadingSceneHost
import io.github.rwx.ui.model.SettingsModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.GlobalContext
import javax.swing.SwingUtilities

object DesktopMain : KoinComponent {
    @JvmStatic
    fun main(args: Array<String>) {
        configureDesktopLogging()
        configureLwjglMemoryStack()
        System.setProperty("org.lwjgl.opengl.contextAPI", "native")
        GameEngine.isMenuBackgroundDisabled = true
        GameEngine.isNonAndroidVersion = true
        GameEngine.isDesktopInitialized = true
        GameEngine.isJavaDesktopVersion = true
        GameEngine.isPCOrIOSVersion = true
        InputController.b = DesktopInputHandler()
        ensureDesktopOpenAlMusicFactory()
        GlobalContext.startKoin { modules(coreModule, desktopModule) }
        (get<CrashReporter>() as? FileCrashReporter)?.installAsDefaultUncaughtExceptionHandler()
        GameFontMetrics.install(DesktopFontMetrics(get<PlatformStorage>()))
        SettingsEngine.getInstance().save()
        val settings = SettingsModel().also {
            get<GameSettingsRepository>().loadInto(it)
            get<GameSettingsRepository>().saveFrom(it)
        }
        LocaleSettings.initialize()
        val options = AppOptions.parseArgs(args, isDesktop = true)
        if (options.colorSchemeId != ColorSchemeRegistry.defaultSchemeId) {
            settings.selectedColorSchemeId.value = options.colorSchemeId
        }
        val bridge = get<PlatformBridge>()
        SwingUtilities.invokeLater {
            val host = SwingAppHost.create(fullscreen = SettingsEngine.getInstance().slick2dFullScreen)
            bridge.filePickerHost = host
            host.closeCompletion.whenComplete { _, error ->
                if (error != null) {
                    logger.error(error) { "Desktop shutdown failed; native frame was not disposed" }
                } else {
                    bridge.filePickerHost = null
                    GlobalContext.stopKoin()
                    kotlin.system.exitProcess(0)
                }
            }
            host.installComposeStartupUi(AppUiState(
                composeEnabled = true,
                colorSchemeId = settings.selectedColorSchemeId.value,
                overlayOpacity = settings.overlayOpacity.value,
                loading = LoadingSceneHost(settings).snapshot(),
            ))
            try {
                val session = installApp(
                    viewportProvider = { host.viewport },
                    scheduler = SwingFrameScheduler(),
                    presenter = SlickFramePresenter { host.presentSnapshot(get<SlickGameSession>().currentSnapshot()) },
                    options = options,
                    onQuit = host::requestClose,
                )
                host.installComposeUi(session)
            } catch (error: Throwable) {
                bridge.filePickerHost = null
                host.dispose()
                throw error
            }
        }
    }
}
