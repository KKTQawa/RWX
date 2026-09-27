package io.github.rwx.app

import io.github.rwx.i18n.I18n
import io.github.rwx.ui.*
import io.github.rwx.ui.host.DialogSceneHost
import io.github.rwx.ui.host.LoadingDialogSceneHost
import io.github.rwx.ui.host.ResourceBrowserSceneHost
import io.github.rwx.ui.model.Dialog
import io.github.rwx.ui.model.LoadingDialogHandle
import io.github.rwx.ui.model.DialogButton
import io.github.rwx.ui.model.ResourceBrowserItem
import io.github.rwx.ui.model.ResourceBrowserSearchResult
import io.github.rwx.ui.model.ResourceBrowserSource
import io.github.rwx.ui.model.ResourceBrowserType
import java.io.File
import java.util.concurrent.atomic.AtomicReference

internal class ResourceBrowserController(
    private val searchResources: (ResourceBrowserSource, ResourceBrowserType, Int, String) -> Result<List<ResourceBrowserItem>>,
    private val downloadResource: suspend (ResourceBrowserItem, (Float) -> Unit) -> Result<File>,
    private val sceneHost: ResourceBrowserSceneHost,
    private val loadingDialogSceneHost: LoadingDialogSceneHost,
    private val dialogSceneHost: DialogSceneHost,
    private val showUnavailableDialog: (String) -> Unit,
    private val onDownloaded: (ResourceBrowserType) -> Unit,
    private val launchSearch: (() -> Unit) -> Unit = { work -> launchOnIO("resource-browser-search") { work() } },
) {
    private var requestId = 0
    private val searchState = AtomicReference<SearchObservation?>(null)
    private val downloadState = AtomicReference<DownloadObservation?>(null)
    private var downloadRequestId = 0L
    private var loadingHandle: LoadingDialogHandle? = null

    fun submitSearch(keyword: String) {
        sceneHost.setKeyword(keyword)
        requestSearch()
    }

    fun requestSearch(append: Boolean = false) {
        val state = sceneHost.currentModel()
        if (append && state.isLoading) return
        val page = if (append) state.page + 1 else 1
        val nextRequestId = ++requestId
        searchState.set(SearchObservation(nextRequestId))
        sceneHost.setLoading(true, I18n.resourcebrowser.loading())
        launchSearch {
            val result = searchResources(state.source, state.type, page, state.keyword)
            val completed = result.fold(
                onSuccess = { items ->
                    ResourceBrowserSearchResult(
                        requestId = nextRequestId,
                        append = append,
                        page = page,
                        items = items,
                    )
                },
                onFailure = { error ->
                    ResourceBrowserSearchResult(
                        requestId = nextRequestId,
                        append = append,
                        page = page,
                        errorMessage = I18n.resourcebrowser.failed(error.message ?: error.javaClass.simpleName),
                    )
                },
            )
            searchState.updateAndGet { current ->
                if (current?.id == nextRequestId) current.copy(result = completed) else current
            }
            CoreUiEventQueue.requestResourceBrowserSearchCompleted()
        }
    }

    fun selectType(type: ResourceBrowserType) {
        if (sceneHost.currentModel().type == type) return
        sceneHost.setType(type)
        requestSearch(append = false)
    }

    fun beginVisit() = sceneHost.beginVisit()

    fun requestInitialSearchIfEmpty() {
        val state = sceneHost.currentModel()
        if (state.items.isEmpty() && !state.isLoading) {
            requestSearch(append = false)
        }
    }

    fun handleSearchCompleted() {
        val result = searchState.getAndUpdate { if (it?.result != null) null else it }?.result ?: return
        if (result.requestId == requestId) {
            sceneHost.applySearchResult(result)
        }
    }

    fun leaveScreen() {
        if (!sceneHost.currentModel().isLoading) return
        requestId++
        searchState.set(null)
        sceneHost.setLoading(false, "")
    }

    fun download(item: ResourceBrowserItem) {
        if (downloadState.get() != null) return
        if (item.downloadUrl.isNullOrBlank()) {
            showUnavailableDialog(I18n.resourcebrowser.missingDownloadUrl())
            return
        }

        val id = ++downloadRequestId
        downloadState.set(DownloadObservation(id))
        val job = launchOnIO("resource-browser-download") {
            if (downloadState.get()?.id != id) return@launchOnIO
            val result = downloadResource(item) { progress ->
                if (progress >= 0.0f) {
                    downloadState.updateAndGet { current -> if (current?.id == id) current.copy(progress = progress) else current }
                    CoreUiEventQueue.requestResourceBrowserDownloadProgress()
                }
            }
            downloadState.updateAndGet { current ->
                if (current?.id == id) current.copy(result = ResourceBrowserDownloadResult(item, result.exceptionOrNull())) else current
            }
            CoreUiEventQueue.requestResourceBrowserDownloadCompleted()
        }
        loadingHandle = loadingDialogSceneHost.showProgress(
            title = I18n.resourcebrowser.downloading(),
            message = item.title,
            progress = 0.01f,
        ) {
            if (downloadState.get()?.id == id) {
                downloadState.set(null)
                hideLoading()
                job.cancel()
            }
        }
    }

    private fun hideLoading() {
        val handle = loadingHandle
        loadingHandle = null
        handle?.let(loadingDialogSceneHost::hide)
    }

    fun handleDownloadProgress() {
        val progress = downloadState.getAndUpdate { it?.copy(progress = null) }?.progress ?: return
        loadingHandle?.let { loadingDialogSceneHost.updateProgress(I18n.resourcebrowser.downloading(), progress, it) }
    }

    fun handleDownloadCompleted() {
        val result = downloadState.getAndUpdate { if (it?.result != null) null else it }?.result ?: return
        hideLoading()
        if (result.error == null) {
            onDownloaded(result.item.type)
            dialogSceneHost.show(
                Dialog(
                    title = I18n.resourcebrowser.downloadDone(),
                    message = I18n.resourcebrowser.downloadDoneMessage(),
                    buttons = listOf(DialogButton(I18n.common.ok())),
                ),
            )
        } else {
            showUnavailableDialog(
                I18n.resourcebrowser.downloadFailed(result.error.message ?: result.error.javaClass.simpleName)
            )
        }
    }
}

private data class SearchObservation(
    val id: Int,
    val result: ResourceBrowserSearchResult? = null,
)

private data class ResourceBrowserDownloadResult(
    val item: ResourceBrowserItem,
    val error: Throwable?,
)

private data class DownloadObservation(
    val id: Long,
    val progress: Float? = null,
    val result: ResourceBrowserDownloadResult? = null,
)
