package io.github.rwx.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.rwx.i18n.I18n
import io.github.rwx.ui.model.ModWindowAction
import io.github.rwx.ui.model.ModWindowUiState
import io.github.rwx.ui.theme.Layout
import io.github.rwx.ui.theme.LocalColorScheme
import io.github.rwx.ui.theme.Spacing

/**
 * The full-screen window a mod opened over the paused game. The mod's Compose content fills the
 * responsive column between the title and the back button; an explicit refresh (new revision)
 * rebuilds that content from scratch, as the previous renderer did.
 */
@Composable
fun ModWindowScreen(state: ModWindowUiState, onAction: (ModWindowAction) -> Unit) {
    val palette = LocalColorScheme.current.palette
    val content = state.content
    val context = state.context
    BoxWithConstraints(Modifier.fillMaxSize().background(palette.panelOverlay).testTag("mod-window")) {
        val compact = maxHeight < Layout.compactHeightBreakpoint
        val padding = if (compact) Spacing.sm else Spacing.xl
        val contentWidth = (maxWidth - padding * 2).coerceAtLeast(1.dp).coerceIn(1.dp, 1180.dp)
        Column(
            Modifier.width(contentWidth).fillMaxHeight().align(Alignment.Center).padding(vertical = padding),
            verticalArrangement = Arrangement.spacedBy(if (compact) Spacing.sm else Spacing.lg),
        ) {
            Text(
                text = if (content != null && context != null) state.title else I18n.modWindow.emptyTitle(),
                style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                color = palette.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().testTag("mod-window-title"),
            )
            // Mod content stacks vertically like the previous panel did; mods add their own scrolling.
            Column(Modifier.fillMaxWidth().weight(1f).testTag("mod-window-content")) {
                if (content != null && context != null) {
                    key(state.revision) { content(context) }
                } else {
                    Text(I18n.modWindow.emptyMessage(), color = palette.textSecondary, modifier = Modifier.testTag("mod-window-empty"))
                }
            }
            OutlinedButton(
                onClick = { onAction(ModWindowAction.Close) },
                modifier = Modifier.fillMaxWidth().heightIn(min = Layout.barMinHeight).testTag("mod-window-back"),
            ) { Text(I18n.modWindow.back()) }
        }
    }
}
