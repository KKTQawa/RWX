package io.github.rwx.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.rwx.ui.model.PauseMenuAction
import io.github.rwx.ui.model.PauseMenuItem
import io.github.rwx.ui.theme.Layout
import io.github.rwx.ui.theme.LocalColorScheme
import io.github.rwx.ui.theme.Spacing

/** The existing pause actions own saving, chat, surrender, and exit confirmation. */
@Composable
fun PauseMenuScreen(items: List<PauseMenuItem>, onAction: (PauseMenuAction) -> Unit) {
    val palette = LocalColorScheme.current.palette
    Box(Modifier.fillMaxSize().background(palette.panelOverlay), contentAlignment = Alignment.Center) {
        Column(
            Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            // Keep Resume reachable even when the remaining actions need scrolling on short screens.
            items.firstOrNull { it.action == PauseMenuAction.Resume }?.let { item ->
                Button(
                    onClick = { onAction(item.action) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = Layout.barMinHeight).testTag("pause-action-${item.action.name}"),
                ) { Text(item.label) }
            }
            Column(
                Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items.filterNot { it.action == PauseMenuAction.Resume }.forEach { item ->
                    val destructive = item.action == PauseMenuAction.Surrender || item.action == PauseMenuAction.ExitGame
                    OutlinedButton(
                        onClick = { onAction(item.action) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = Layout.barMinHeight).testTag("pause-action-${item.action.name}"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (destructive) palette.danger else palette.primary),
                    ) { Text(item.label) }
                }
            }
        }
    }
}
