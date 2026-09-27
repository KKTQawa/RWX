package io.github.rwx.ui.host

import io.github.rwx.i18n.I18n
import io.github.rwx.ui.model.ResourceBrowserAction
import io.github.rwx.ui.model.ResourceBrowserModel
import io.github.rwx.ui.model.ResourceBrowserSearchResult
import io.github.rwx.ui.model.ResourceBrowserType

class ResourceBrowserSceneHost(
    private val onAction: (ResourceBrowserAction) -> Unit = {},
) {
    private var browserModel = ResourceBrowserModel()

    fun currentModel(): ResourceBrowserModel = browserModel

    fun setKeyword(keyword: String) {
        browserModel = browserModel.copy(
            keyword = keyword.trim(), page = 0, items = emptyList(), statusText = "",
            revision = browserModel.revision + 1,
        )
    }

    fun beginVisit() {
        browserModel = browserModel.copy(revision = browserModel.revision + 1)
    }

    fun setLoading(loading: Boolean, statusText: String = browserModel.statusText) {
        browserModel = browserModel.copy(
            isLoading = loading, statusText = statusText,
            revision = browserModel.revision + 1,
        )
    }

    fun setType(type: ResourceBrowserType) {
        browserModel = browserModel.copy(
            type = type, page = 0, items = emptyList(), statusText = "",
            revision = browserModel.revision + 1,
        )
    }

    fun applySearchResult(result: ResourceBrowserSearchResult) {
        val current = browserModel
        val items = (if (result.append) current.items + result.items else result.items)
            .associateBy { it.type to it.id }.values.toList()
        browserModel = current.copy(
            items = if (result.errorMessage == null) items else current.items,
            page = if (result.errorMessage == null) result.page else current.page,
            isLoading = false,
            statusText = result.errorMessage ?: if (items.isEmpty()) I18n.resourcebrowser.empty() else "",
            revision = current.revision + 1,
        )
    }

    fun dispatch(action: ResourceBrowserAction) = onAction(action)
}
