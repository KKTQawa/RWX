package io.github.rwx.ui.model

import io.github.rwx.i18n.I18n
import io.github.rwx.ui.AppScreen

enum class ResourceBrowserType {
    Mod,
    Map;

    val label: String
        get() = when (this) {
            Mod -> I18n.resourcebrowser.typeMod()
            Map -> I18n.resourcebrowser.typeMap()
        }
}

enum class ResourceBrowserSource {
    RtsBoxSearch,
    RtsBoxWeeklyDownloads,
    ;

    val displayName: String
        get() = when (this) {
            RtsBoxSearch -> I18n.resourcebrowser.sourceSearch()
            RtsBoxWeeklyDownloads -> I18n.resourcebrowser.sourceWeekly()
        }
}

data class ResourceBrowserItem(
    val id: String,
    val title: String,
    val type: ResourceBrowserType,
    val sourceName: String,
    val description: String? = null,
    val downloadCount: Int? = null,
    val bbsUrl: String? = null,
    val downloadUrl: String? = null,
    val version: String? = null,
    val author: String? = null,
    val imageUrl: String? = null,
)

data class ResourceBrowserModel(
    val items: List<ResourceBrowserItem> = emptyList(),
    val type: ResourceBrowserType = ResourceBrowserType.Mod,
    val keyword: String = "",
    /** Last successfully loaded page for this query; zero until its first result succeeds. */
    val page: Int = 0,
    val isLoading: Boolean = false,
    val statusText: String = "",
    val revision: Long = 0,
) {
    val source: ResourceBrowserSource
        get() = resourceBrowserSourceForFilter(keyword)

    internal fun resolveAction(requestRevision: Long, action: ResourceBrowserAction): ResourceBrowserAction? {
        if (action is ResourceBrowserAction.Back || action is ResourceBrowserAction.SubmitSearch ||
            action is ResourceBrowserAction.SelectType) return action
        if (requestRevision != revision) return null
        return when (action) {
            ResourceBrowserAction.LoadMore -> action.takeUnless { isLoading }
            is ResourceBrowserAction.OpenLink -> items.singleOrNull { it.id == action.item.id && it.id.isNotBlank() }
                ?.takeIf { it == action.item && !it.bbsUrl.isNullOrBlank() }?.let(ResourceBrowserAction::OpenLink)
            is ResourceBrowserAction.Download -> items.singleOrNull { it.id == action.item.id && it.id.isNotBlank() }
                ?.takeIf { it == action.item && !it.downloadUrl.isNullOrBlank() }?.let(ResourceBrowserAction::Download)
            else -> action
        }
    }
}

internal fun resourceBrowserSourceForFilter(filter: String): ResourceBrowserSource =
    if (filter.isBlank()) ResourceBrowserSource.RtsBoxWeeklyDownloads else ResourceBrowserSource.RtsBoxSearch

sealed interface ResourceBrowserAction {
    data object Back : ResourceBrowserAction
    data object Search : ResourceBrowserAction
    data class SubmitSearch(val keyword: String) : ResourceBrowserAction
    data object LoadMore : ResourceBrowserAction
    data class SelectType(val type: ResourceBrowserType) : ResourceBrowserAction
    data class OpenLink(val item: ResourceBrowserItem) : ResourceBrowserAction
    data class Download(val item: ResourceBrowserItem) : ResourceBrowserAction
}

sealed interface ResourceBrowserOutcome {
    data class Navigate(val screen: AppScreen) : ResourceBrowserOutcome
    data object SearchRequested : ResourceBrowserOutcome
    data class SearchSubmitted(val keyword: String) : ResourceBrowserOutcome
    data object LoadMoreRequested : ResourceBrowserOutcome
    data class TypeSelected(val type: ResourceBrowserType) : ResourceBrowserOutcome
    data class OpenLink(val item: ResourceBrowserItem) : ResourceBrowserOutcome
    data class Download(val item: ResourceBrowserItem) : ResourceBrowserOutcome
}

object ResourceBrowserNavigation {
    fun outcomeFor(action: ResourceBrowserAction): ResourceBrowserOutcome = when (action) {
        ResourceBrowserAction.Back -> ResourceBrowserOutcome.Navigate(AppScreen.MainMenu)
        ResourceBrowserAction.Search -> ResourceBrowserOutcome.SearchRequested
        is ResourceBrowserAction.SubmitSearch -> ResourceBrowserOutcome.SearchSubmitted(action.keyword)
        ResourceBrowserAction.LoadMore -> ResourceBrowserOutcome.LoadMoreRequested
        is ResourceBrowserAction.SelectType -> ResourceBrowserOutcome.TypeSelected(action.type)
        is ResourceBrowserAction.OpenLink -> ResourceBrowserOutcome.OpenLink(action.item)
        is ResourceBrowserAction.Download -> ResourceBrowserOutcome.Download(action.item)
    }
}

data class ResourceBrowserSearchResult(
    val requestId: Int,
    val append: Boolean,
    val page: Int,
    val items: List<ResourceBrowserItem> = emptyList(),
    val errorMessage: String? = null,
)

internal fun resourceBrowserColumnCount(isAndroidPlatform: Boolean): Int =
    if (isAndroidPlatform) 2 else 1
