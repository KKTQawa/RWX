package io.github.rwx.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.rwx.i18n.I18n
import io.github.rwx.ui.component.itemAppear
import io.github.rwx.ui.component.pressScale
import io.github.rwx.ui.component.withStableKeys
import io.github.rwx.ui.model.ReplayBrowser
import io.github.rwx.ui.model.ReplayEntry
import io.github.rwx.ui.model.ReplaySelectAction
import io.github.rwx.ui.model.ReplaySelectUiState
import io.github.rwx.ui.theme.Corners
import io.github.rwx.ui.theme.Layout
import io.github.rwx.ui.theme.LocalColorScheme
import io.github.rwx.ui.theme.Spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReplaySelectScreen(
    state: ReplaySelectUiState,
    onAction: (ReplaySelectAction) -> Unit,
    enableAnimations: Boolean = true,
) {
    var filter by rememberSaveable { mutableStateOf("") }
    val visible = remember(state.replays, filter) {
        val allowed = ReplayBrowser.visibleReplays(state.replays, filter).toSet()
        state.replays.withIndex().filter { it.value in allowed }
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val palette = LocalColorScheme.current.palette

    BoxWithConstraints(Modifier.fillMaxSize().background(palette.panelOverlay)) {
        val padding = if (maxHeight < Layout.compactHeightBreakpoint) Spacing.sm else Spacing.xl
        Column(
            Modifier.widthIn(max = 1120.dp).fillMaxSize().align(Alignment.Center).padding(padding),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedButton(onClick = { onAction(ReplaySelectAction.Back) }, modifier = Modifier.testTag("replay-back")) {
                    Text(I18n.common.back())
                }
                OutlinedTextField(
                    value = filter,
                    onValueChange = {
                        filter = it
                        scope.launch { listState.scrollToItem(0) }
                    },
                    label = { Text(I18n.replay.searchHint()) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("replay-query"),
                )
            }
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                if (visible.isEmpty()) {
                    Text(
                        if (state.replays.isEmpty()) I18n.replay.empty() else I18n.replay.noMatches(),
                        color = palette.textSecondary,
                        modifier = Modifier.testTag("replay-empty"),
                    )
                } else {
                    val replayKeys = remember(visible) {
                        visible.withStableKeys { "${it.value.fileName}:${it.value.replayName}" }
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().testTag("replay-list"),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        contentPadding = PaddingValues(bottom = Spacing.sm),
                    ) {
                        itemsIndexed(visible, key = { index, _ -> replayKeys[index] }) { _, indexed ->
                            Box(Modifier.then(if (enableAnimations) Modifier.animateItem() else Modifier).then(Modifier.itemAppear(enableAnimations))) {
                                ReplayCard(indexed.index, indexed.value, enableAnimations) { onAction(ReplaySelectAction.SelectReplay(indexed.value)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplayCard(index: Int, replay: ReplayEntry, enableAnimations: Boolean = true, onClick: () -> Unit) {
    val palette = LocalColorScheme.current.palette
    Column(
        Modifier.fillMaxWidth().heightIn(min = 68.dp)
            .background(palette.surfaceSunken, RoundedCornerShape(Corners.sm))
            .border(1.dp, palette.borderSubtle, RoundedCornerShape(Corners.sm))
            .testTag("replay:$index:${replay.fileName}")
            .clickable(role = Role.Button, onClick = onClick)
            .padding(Spacing.md)
            .pressScale(enableAnimations),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(replay.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text(replay.modifiedAt, color = palette.textSecondary, style = MaterialTheme.typography.bodySmall)
            Text(replay.sizeLabel, color = palette.textSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}
