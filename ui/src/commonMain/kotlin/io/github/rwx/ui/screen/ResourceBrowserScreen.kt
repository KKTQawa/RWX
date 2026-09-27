package io.github.rwx.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.rwx.i18n.I18n
import io.github.rwx.ui.component.itemAppear
import io.github.rwx.ui.component.withStableKeys
import io.github.rwx.ui.model.*
import io.github.rwx.ui.theme.Corners
import io.github.rwx.ui.theme.Layout
import io.github.rwx.ui.theme.LocalColorScheme
import io.github.rwx.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ResourceBrowserScreen(
    state: ResourceBrowserModel,
    onAction: (ResourceBrowserAction) -> Unit,
    previewLoader: ResourcePreviewLoader = defaultResourcePreviewLoader,
    enableAnimations: Boolean = true,
) {
    var draft by rememberSaveable { mutableStateOf(state.keyword) }
    var snapshotKeyword by rememberSaveable { mutableStateOf(state.keyword) }
    val listState = rememberLazyListState()
    LaunchedEffect(state.keyword) {
        if (state.keyword != snapshotKeyword) {
            snapshotKeyword = state.keyword
            draft = state.keyword
            listState.scrollToItem(0)
        }
    }
    val scope = rememberCoroutineScope()
    var snapshotType by rememberSaveable { mutableStateOf(state.type) }
    LaunchedEffect(state.type) {
        if (state.type != snapshotType) {
            snapshotType = state.type
            listState.scrollToItem(0)
        }
    }
    val palette = LocalColorScheme.current.palette
    val submit = { onAction(ResourceBrowserAction.SubmitSearch(draft)) }
    var pullRefreshing by remember { mutableStateOf(false) }
    var sawLoading by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()
    val triggerRefresh = {
        pullRefreshing = true
        onAction(ResourceBrowserAction.Search)
    }
    LaunchedEffect(state.isLoading) {
        if (state.isLoading) {
            sawLoading = true
        } else if (sawLoading) {
            pullRefreshing = false
            sawLoading = false
        }
    }
    LaunchedEffect(pullRefreshing) {
        if (pullRefreshing && !state.isLoading && !sawLoading) {
            delay(10_000.milliseconds)
            if (pullRefreshing && !state.isLoading && !sawLoading) {
                pullRefreshing = false
            }
        }
    }
    LaunchedEffect(listState, state.isLoading) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && state.items.isNotEmpty() && !state.isLoading &&
                    lastIndex >= state.items.size - 3 && lastIndex >= 5
                ) {
                    onAction(ResourceBrowserAction.LoadMore)
                }
            }
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(palette.panelOverlay)) {
        val padding = if (maxHeight < Layout.compactHeightBreakpoint) Spacing.sm else Spacing.xl
        Column(
            Modifier.widthIn(max = 1280.dp).fillMaxSize().align(Alignment.Center).padding(padding),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedButton(onClick = { onAction(ResourceBrowserAction.Back) }, modifier = Modifier.testTag("resource-back")) {
                    Text(I18n.common.back())
                }
                ResourceBrowserType.entries.forEach { type ->
                    FilterChip(
                        selected = state.type == type,
                        onClick = { onAction(ResourceBrowserAction.SelectType(type)) },
                        label = { Text(type.label) },
                        modifier = Modifier.testTag("resource-type-${type.name}"),
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = {
                        draft = it
                        scope.launch { listState.scrollToItem(0) }
                    },
                    label = { Text(I18n.resourcebrowser.searchHint()) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { submit() }),
                    modifier = Modifier.weight(1f).testTag("resource-query"),
                )
                Button(onClick = submit, modifier = Modifier.testTag("resource-search")) { Text(I18n.resourcebrowser.search()) }
                OutlinedButton(
                    onClick = triggerRefresh,
                    enabled = !pullRefreshing && !state.isLoading,
                    modifier = Modifier.testTag("resource-refresh"),
                ) { Text(I18n.resourcebrowser.refresh()) }
            }
            if (state.isLoading && !pullRefreshing) {
                LinearProgressIndicator(Modifier.fillMaxWidth().testTag("resource-loading"))
            }
            PullToRefreshBox(
                isRefreshing = pullRefreshing,
                onRefresh = triggerRefresh,
                state = pullState,
                modifier = Modifier.fillMaxWidth().weight(1f).testTag("resource-pull-refresh"),
            ) {
                if (state.items.isEmpty()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().testTag("resource-list"),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        contentPadding = PaddingValues(bottom = Spacing.sm),
                    ) {
                        item {
                            Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    state.statusText.ifBlank { if (state.isLoading) I18n.resourcebrowser.loading() else I18n.resourcebrowser.empty() },
                                    color = palette.textSecondary,
                                    modifier = Modifier.testTag("resource-status"),
                                )
                            }
                        }
                    }
                } else {
                    val resourceKeys = remember(state.items) { state.items.withStableKeys { it.id } }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().testTag("resource-list"),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        contentPadding = PaddingValues(bottom = Spacing.sm),
                    ) {
                        itemsIndexed(state.items, key = { index, _ -> resourceKeys[index] }) { index, item ->
                            Box(Modifier.then(if (enableAnimations) Modifier.animateItem() else Modifier).then(Modifier.itemAppear(enableAnimations))) {
                                ResourceCard(index, item, state.revision, previewLoader, onAction)
                            }
                        }
                    }
                }
            }
            if (state.items.isNotEmpty() && state.statusText.isNotBlank()) {
                Text(state.statusText, color = if (state.isLoading) palette.textSecondary else palette.danger, modifier = Modifier.testTag("resource-status"))
            }
        }
    }
}

@Composable
private fun ResourceCard(
    index: Int,
    item: ResourceBrowserItem,
    revision: Long,
    previewLoader: ResourcePreviewLoader,
    onAction: (ResourceBrowserAction) -> Unit,
) {
    val palette = LocalColorScheme.current.palette
    Column(
        Modifier.fillMaxWidth()
            .background(palette.surfaceSunken, RoundedCornerShape(Corners.sm))
            .border(1.dp, palette.borderSubtle, RoundedCornerShape(Corners.sm))
            .testTag("resource:$index:${item.id}")
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.tight),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            item.imageUrl?.takeIf { it.isNotBlank() }?.let { imageUrl ->
                LibraryPreviewImage(
                    source = imageUrl,
                    revision = revision,
                    imageTag = "resource-preview",
                    modifier = Modifier.size(84.dp),
                    loader = previewLoader,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(item.title, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
                Text(item.sourceName, color = palette.textSecondary, style = MaterialTheme.typography.bodySmall)
                Text(item.type.label, color = palette.textSecondary, style = MaterialTheme.typography.bodySmall)
                item.author?.takeIf { it.isNotBlank() }?.let { Text(I18n.resourcebrowser.author(it), color = palette.textSecondary, style = MaterialTheme.typography.bodySmall) }
                item.version?.takeIf { it.isNotBlank() }?.let { Text(I18n.resourcebrowser.version(it), color = palette.textSecondary, style = MaterialTheme.typography.bodySmall) }
                item.downloadCount?.let { Text(I18n.resourcebrowser.downloads(it), color = palette.textSecondary, style = MaterialTheme.typography.bodySmall) }
            }
        }
        item.description?.takeIf { it.isNotBlank() }?.let {
            Text(it, color = palette.textSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
        if (!item.bbsUrl.isNullOrBlank() || !item.downloadUrl.isNullOrBlank()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (!item.bbsUrl.isNullOrBlank()) {
                    TextButton(onClick = { onAction(ResourceBrowserAction.OpenLink(item)) }, modifier = Modifier.testTag("resource-open:$index")) {
                        Text(I18n.resourcebrowser.open())
                    }
                }
                if (!item.downloadUrl.isNullOrBlank()) {
                    TextButton(onClick = { onAction(ResourceBrowserAction.Download(item)) }, modifier = Modifier.testTag("resource-download:$index")) {
                        Text(I18n.resourcebrowser.download())
                    }
                }
            }
        }
    }
}
