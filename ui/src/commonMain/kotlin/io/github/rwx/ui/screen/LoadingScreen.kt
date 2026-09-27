package io.github.rwx.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.rwx.ui.model.LoadingUiState
import io.github.rwx.ui.theme.Layout
import io.github.rwx.ui.theme.LocalColorScheme
import io.github.rwx.ui.theme.Spacing

@Composable
fun LoadingScreen(state: LoadingUiState) {
    val scheme = LocalColorScheme.current
    val progress = state.progress
    BoxWithConstraints(Modifier.fillMaxSize().background(scheme.palette.surfaceBase).testTag("startup-loading")) {
        val compact = maxHeight < Layout.compactHeightBreakpoint
        val showHeader = maxHeight >= 320.dp
        val contentWidth = (maxWidth - Spacing.xxl).coerceAtLeast(1.dp).coerceAtMost(640.dp)
        Column(
            Modifier.width(contentWidth).align(Alignment.Center)
                .verticalScroll(rememberScrollState()).padding(vertical = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            if (showHeader) MainMenuHeader(contentWidth, scheme, compact)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp)
                Text(
                    state.text,
                    modifier = Modifier.weight(1f).testTag("startup-loading-status"),
                    color = scheme.palette.textPrimary,
                    maxLines = if (compact) 2 else 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (progress == null) {
                LinearProgressIndicator(Modifier.fillMaxWidth().testTag("startup-loading-indeterminate"))
            } else {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().testTag("startup-loading-progress"),
                )
            }
            state.completedSteps.forEach { step ->
                Text(step, Modifier.fillMaxWidth(), color = scheme.palette.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
