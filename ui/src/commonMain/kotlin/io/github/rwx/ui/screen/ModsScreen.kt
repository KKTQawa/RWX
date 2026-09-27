package io.github.rwx.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.rwx.i18n.I18n
import io.github.rwx.ui.component.itemAppear
import io.github.rwx.ui.component.Icon
import io.github.rwx.ui.model.ModUiEntry
import io.github.rwx.ui.model.ModsAction
import io.github.rwx.ui.model.ModsUiState
import io.github.rwx.ui.theme.Corners
import io.github.rwx.ui.theme.Layout
import io.github.rwx.ui.theme.LocalColorScheme
import io.github.rwx.ui.theme.Spacing
import kotlinx.coroutines.launch

@Composable
fun ModsScreen(
    state: ModsUiState,
    onAction: (ModsAction) -> Unit,
    previewLoader: ModPreviewLoader = defaultModPreviewLoader,
    enableAnimations: Boolean = true,
) {
    var filter by rememberSaveable { mutableStateOf("") }
    val visible = remember(state.mods, filter) {
        val query = filter.trim()
        state.mods.withIndex().filter { query.isEmpty() || it.value.name.contains(query, ignoreCase = true) }
    }
    val enabled = remember(visible) { visible.filter { it.value.isEnabled } }
    val disabled = remember(visible) { visible.filter { !it.value.isEnabled } }
    val ambiguousIds = remember(state.mods) {
        state.mods.groupingBy { it.id }.eachCount().filter { it.key.isBlank() || it.value != 1 }.keys
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val palette = LocalColorScheme.current.palette

    BoxWithConstraints(Modifier.fillMaxSize().background(palette.panelOverlay)) {
        val compact = maxWidth < 720.dp
        val padding = if (maxHeight < Layout.compactHeightBreakpoint) Spacing.sm else Spacing.xl
        Column(
            Modifier.widthIn(max = 1280.dp).fillMaxSize().align(Alignment.Center).padding(padding),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedButton(onClick = { onAction(ModsAction.Back) }, modifier = Modifier.testTag("mods-back")) {
                    Text(I18n.common.back())
                }
                OutlinedTextField(
                    value = filter,
                    onValueChange = {
                        filter = it
                        scope.launch { listState.scrollToItem(0) }
                    },
                    singleLine = true,
                    label = { Text(I18n.mods.filter()) },
                    modifier = Modifier.weight(1f).testTag("mods-filter"),
                )
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().weight(1f).testTag("mods-list"),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                contentPadding = PaddingValues(bottom = Spacing.sm),
            ) {
                if (state.mods.isEmpty()) {
                    item { EmptyLibraryMessage(I18n.mods.empty(), "mods-empty") }
                } else {
                    item { SectionTitle(I18n.mods.enabled()) }
                    items(enabled, key = { "enabled:${it.value.id}" }) { indexed ->
                        Box(Modifier.then(if (enableAnimations) Modifier.animateItem() else Modifier).then(Modifier.itemAppear(enableAnimations))) {
                            ModCard(indexed.index, indexed.value, compact, indexed.value.id !in ambiguousIds, state.revision, previewLoader, onAction)
                        }
                    }
                    item { SectionTitle(I18n.mods.disabled()) }
                    items(disabled, key = { "disabled:${it.value.id}" }) { indexed ->
                        Box(Modifier.then(if (enableAnimations) Modifier.animateItem() else Modifier).then(Modifier.itemAppear(enableAnimations))) {
                            ModCard(indexed.index, indexed.value, compact, indexed.value.id !in ambiguousIds, state.revision, previewLoader, onAction)
                        }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(end = Layout.pageBottomReserve),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                OutlinedButton(
                    onClick = { onAction(ModsAction.Reload) },
                    modifier = Modifier.testTag("mods-reload")
                ) { Text(I18n.mods.reload()) }
                OutlinedButton(
                    onClick = { onAction(ModsAction.ImportFile) },
                    modifier = Modifier.testTag("mods-import")
                ) { Text(I18n.mods.importFile()) }
                OutlinedButton(
                    onClick = { onAction(ModsAction.DisableAll) },
                    modifier = Modifier.testTag("mods-disable-all")
                ) { Text(I18n.mods.disableAll()) }
            }
        }
        Box(Modifier.widthIn(max = 1280.dp).fillMaxSize().align(Alignment.Center)) {
            FloatingActionButton(
                onClick = { onAction(ModsAction.Apply) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(Spacing.lg).testTag("mods-apply"),
                shape = CircleShape,
                containerColor = palette.primaryContainer,
                contentColor = palette.onPrimary,
            ) {
                Icon(Icon.Apply, Layout.contentIconSize, palette.onPrimary)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = LocalColorScheme.current.palette.primary, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun EmptyLibraryMessage(text: String, tag: String) {
    Box(Modifier.fillMaxWidth().padding(Spacing.xl), contentAlignment = Alignment.Center) {
        Text(text, color = LocalColorScheme.current.palette.textSecondary, modifier = Modifier.testTag(tag))
    }
}

@Composable
private fun ModCard(
    index: Int,
    mod: ModUiEntry,
    compact: Boolean,
    hasUniqueId: Boolean,
    revision: Long,
    previewLoader: ModPreviewLoader,
    onAction: (ModsAction) -> Unit,
) {
    val palette = LocalColorScheme.current.palette
    val error = mod.errorMessage?.takeIf { it.isNotBlank() }
    Column(
        Modifier.fillMaxWidth()
            .background(palette.surfaceSunken, RoundedCornerShape(Corners.sm))
            .border(if (error == null) 1.dp else 2.dp, if (error == null) palette.borderSubtle else palette.danger, RoundedCornerShape(Corners.sm))
            .testTag("mod:$index:${mod.id}")
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.tight),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            mod.thumbnail?.takeIf { it.isNotBlank() }?.let { thumbnail ->
                val entryName = thumbnail.replace('\\', '/').trimStart('/')
                val entryLoader: suspend (String) -> androidx.compose.ui.graphics.ImageBitmap? =
                    remember(mod.path, previewLoader) { { entry -> previewLoader(mod.path, entry) } }
                LibraryPreviewImage(
                    source = entryName,
                    revision = revision,
                    imageTag = "mod-preview",
                    modifier = Modifier.size(if (compact) 56.dp else 76.dp),
                    loader = entryLoader,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(mod.name, maxLines = 2, overflow = TextOverflow.Ellipsis)
                mod.author?.takeIf { it.isNotBlank() }?.let { Text(it, color = palette.textSecondary, style = MaterialTheme.typography.bodySmall) }
                mod.version?.takeIf { it.isNotBlank() }?.let { Text(it, color = palette.textSecondary, style = MaterialTheme.typography.bodySmall) }
            }
        }
        mod.description.takeIf { it.isNotBlank() }?.let { description ->
            ExpandableModText(description, palette.textSecondary, "mods-description:$index", hasUniqueId) {
                onAction(ModsAction.ShowDescription(mod.id))
            }
        }
        error?.let {
            ExpandableModText(it, palette.danger, "mods-error:$index", hasUniqueId) {
                onAction(ModsAction.ShowError(mod.id))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(
                enabled = hasUniqueId,
                onClick = { onAction(ModsAction.ToggleEnable(mod.id)) },
                modifier = Modifier.testTag("mods-toggle:$index"),
            ) { Text(if (mod.isEnabled) I18n.mods.disable() else I18n.mods.enable()) }
            TextButton(
                enabled = hasUniqueId,
                onClick = { onAction(ModsAction.Delete(mod.id)) },
                modifier = Modifier.testTag("mods-delete:$index"),
            ) { Text(I18n.mods.delete()) }
        }
    }
}

@Composable
private fun ExpandableModText(text: String, color: androidx.compose.ui.graphics.Color, tag: String, enabled: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Text(text, Modifier.weight(1f), color = color, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
        TextButton(enabled = enabled, onClick = onClick, modifier = Modifier.testTag(tag)) { Text(I18n.mods.more()) }
    }
}
