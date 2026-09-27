package io.github.rwx.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.rwx.i18n.I18n
import io.github.rwx.ui.component.MapPreviewImage
import io.github.rwx.ui.component.MapPreviewLoader
import io.github.rwx.ui.component.defaultMapPreviewLoader
import io.github.rwx.ui.component.itemAppear
import io.github.rwx.ui.component.pressScale
import io.github.rwx.ui.model.*
import io.github.rwx.ui.theme.Corners
import io.github.rwx.ui.theme.Layout
import io.github.rwx.ui.theme.LocalColorScheme
import io.github.rwx.ui.theme.Spacing

/** The browser only renders and filters the shared model; loading and navigation stay in core. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LevelSelectScreen(
    state: LevelSelectUiState,
    onAction: (LevelSelectAction) -> Unit,
    previewLoader: MapPreviewLoader = defaultMapPreviewLoader,
    enableAnimations: Boolean = true,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var playerCount by rememberSaveable(state.currentMode) { mutableStateOf<Int?>(null) }
    var rwxOnly by rememberSaveable(state.currentMode) { mutableStateOf(false) }
    var sort by rememberSaveable { mutableStateOf(LevelSelectSortOption.Default) }
    val filters = remember(state.maps) { LevelSelectMapBrowser.filterOptions(state.maps) }
    val filter = filters.firstOrNull { it.playerCount == playerCount && it.rwxOnly == rwxOnly }
        ?: LevelSelectFilterOption.All
    LaunchedEffect(state.isLoading, filters) {
        if (!state.isLoading && filters.none { it.playerCount == playerCount && it.rwxOnly == rwxOnly }) {
            playerCount = null
            rwxOnly = false
        }
    }
    val visibleMaps = remember(state.maps, query, filter, sort) {
        LevelSelectMapBrowser.visibleMaps(state.maps, query, filter, sort)
    }
    val gridState = rememberLazyGridState()
    LaunchedEffect(state.revision, query, filter, sort) { gridState.scrollToItem(0) }
    val palette = LocalColorScheme.current.palette
    val loadError = state.loadError

    BoxWithConstraints(Modifier.fillMaxSize().background(palette.panelOverlay)) {
        val shortLandscape = maxWidth > maxHeight && maxHeight < Layout.shortHeightBreakpoint
        Column(
            Modifier.widthIn(max = Layout.mainMenuMaxContentWidth).fillMaxSize().align(Alignment.Center)
                .padding(if (shortLandscape) Spacing.sm else Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                OutlinedButton(onClick = { onAction(LevelSelectAction.Back) }, modifier = Modifier.testTag("level-select-back")) {
                    Text(I18n.common.back())
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(I18n.levelselect.searchHint()) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("level-select-query"),
                )
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                BrowserOption(I18n.levelselect.filter.mapType(), state.title, "mode", state.availableModes,
                    label = { it.label }, selected = { it == state.currentMode },
                    onSelect = { onAction(LevelSelectAction.SelectMode(it)) })
                BrowserOption(I18n.levelselect.players(), filter.label, "players", filters,
                    label = { it.label }, selected = { it == filter },
                    onSelect = { playerCount = it.playerCount; rwxOnly = it.rwxOnly })
                BrowserOption(I18n.levelselect.sort.title(), sort.toString(), "sort", LevelSelectSortOption.entries,
                    label = { it.toString() }, selected = { it == sort }, onSelect = { sort = it })
            }
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                when {
                    state.isLoading -> CircularProgressIndicator(Modifier.testTag("level-select-loading"))
                    loadError != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(loadError, color = palette.danger, modifier = Modifier.testTag("level-select-error"))
                        OutlinedButton(
                            onClick = { onAction(LevelSelectAction.SelectMode(state.currentMode)) },
                            modifier = Modifier.testTag("level-select-retry"),
                        ) { Text(I18n.common.retry()) }
                    }
                    visibleMaps.isEmpty() -> Text(
                        if (state.currentMode == LevelSelectMode.SavedGames) I18n.levelselect.emptySavedGames() else I18n.levelselect.emptyNoMatch(),
                        color = palette.textSecondary,
                        modifier = Modifier.testTag("level-select-empty"),
                    )
                    else -> LazyVerticalGrid(
                        columns = GridCells.Adaptive(if (shortLandscape) 148.dp else 252.dp),
                        state = gridState,
                        modifier = Modifier.fillMaxSize().testTag("level-select-grid"),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        contentPadding = PaddingValues(bottom = Spacing.sm),
                    ) {
                        // The index also distinguishes duplicate paths returned by legacy mod listings.
                        itemsIndexed(visibleMaps, key = { _, map -> map.mapAssetPath }) { _, map ->
                            Box(Modifier.then(if (enableAnimations) Modifier.animateItem() else Modifier).then(Modifier.itemAppear(enableAnimations))) {
                                MapTile(map, state.revision, shortLandscape, previewLoader, enableAnimations) { onAction(LevelSelectAction.SelectMap(map)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> BrowserOption(
    title: String,
    value: String,
    tag: String,
    options: List<T>,
    label: (T) -> String,
    selected: (T) -> Boolean,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.testTag("level-select-$tag")) {
            Text("$title: $value")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(label(option)) },
                    onClick = { expanded = false; onSelect(option) },
                    leadingIcon = { RadioButton(selected(option), onClick = null) },
                    modifier = Modifier.testTag("level-select-$tag-option-$index"),
                )
            }
        }
    }
}

@Composable
private fun MapTile(map: MapEntry, revision: Long, compact: Boolean, previewLoader: MapPreviewLoader, enableAnimations: Boolean = true, onClick: () -> Unit) {
    val palette = LocalColorScheme.current.palette
    Column(
        Modifier.fillMaxWidth().height(if (compact) 160.dp else 204.dp)
            .background(palette.surfaceSunken, RoundedCornerShape(Corners.sm))
            .border(1.dp, palette.borderSubtle, RoundedCornerShape(Corners.sm))
            .testTag("map:${map.mapAssetPath}")
            .clickable(role = Role.Button, onClick = onClick).padding(Spacing.xs)
            .pressScale(enableAnimations),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().height(if (compact) 94.dp else 138.dp)) {
            MapPreviewImage(map.previewAssetPath, revision, Modifier.fillMaxSize(), previewLoader)
            map.ModeLabel()?.let { label ->
                Text(
                    label,
                    color = palette.onPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.TopEnd).background(palette.primary).padding(Spacing.xs),
                )
            }
        }
        Text(
            map.displayName,
            modifier = Modifier.fillMaxWidth().height(Layout.compactMenuButtonHeight),
            color = palette.textPrimary,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(map.playerLabel(), color = palette.textSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}
