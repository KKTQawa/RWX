package io.github.rwx.ui.host

import com.corrodinggames.rts.gameFramework.GameEngine
import io.github.rwx.i18n.I18n
import io.github.rwx.ui.model.*
import kotlinx.coroutines.*

/**
 * Frontend-owned Level Select state. Compose renders this screen from [snapshot];
 * map loading runs off the UI thread with a revision guard so stale results are dropped.
 */
class LevelSelectSceneHost(
    private val model: SettingsModel = SettingsModel(),
    private val viewModelFactory: LevelSelectViewModelFactory,
    private val onAction: LevelSelectActionHandler = LevelSelectActionHandler {},
) {
    private val loadScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var closed = false
    private val maps = mutableListOf<MapEntry>()
    private var modeTitle: String = ""
    private var currentMode: LevelSelectMode = LevelSelectMode.Skirmish
    private var isLoading: Boolean = false
    private var loadError: String? = null
    private var mapLoadJob: Job? = null
    private var mapLoadRevision: Long = 0L

    fun updateMaps(mode: LevelSelectMode) {
        if (closed) return
        val revision = ++mapLoadRevision
        mapLoadJob?.cancel()
        currentMode = mode
        modeTitle = mode.label
        isLoading = true
        loadError = null
        synchronized(maps) { maps.clear() }

        mapLoadJob = loadScope.launch {
            try {
                val viewModel = viewModelFactory.create(mode)
                val entries = withContext(Dispatchers.IO) { viewModel.items() }
                if (closed || revision != mapLoadRevision) return@launch
                synchronized(maps) {
                    maps.clear()
                    maps.addAll(entries)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (!closed && revision == mapLoadRevision) {
                    GameEngine.log("Failed to load maps for ${mode.name}: ${error.message}")
                    synchronized(maps) { maps.clear() }
                    loadError = I18n.levelselect.loadError()
                }
            } finally {
                if (!closed && revision == mapLoadRevision) {
                    isLoading = false
                }
            }
        }
    }

    fun snapshot(): LevelSelectUiState = LevelSelectUiState(
        revision = mapLoadRevision,
        currentMode = currentMode,
        maps = synchronized(maps) { maps.toList() },
        isLoading = isLoading,
        loadError = loadError,
    )

    fun dispatch(action: LevelSelectAction) { if (!closed) onAction.onAction(action) }

    /** Retires this frontend owner; neither queued loads nor later actions can reopen it. */
    fun close() {
        if (closed) return
        closed = true
        mapLoadRevision++
        mapLoadJob?.cancel()
        mapLoadJob = null
        loadScope.cancel()
        synchronized(maps) { maps.clear() }
        isLoading = false
        loadError = null
    }

    companion object {
        const val LEVEL_SELECT_SCENE_NAME: String = "level-select"
    }
}
