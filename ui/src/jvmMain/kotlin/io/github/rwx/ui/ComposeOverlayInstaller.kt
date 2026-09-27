package io.github.rwx.ui

import androidx.compose.ui.awt.ComposePanel
import io.github.rwx.app.AppSession
import io.github.rwx.ui.model.LoadingUiState
import io.github.rwx.ui.platform.createDesktopComposeHost
import java.awt.Component
import javax.swing.JFrame
import javax.swing.SwingUtilities

/** Install into the application's frame before bootstrap, then attach its session. */
fun installComposeOverlay(
    owner: JFrame,
    fallbackFocus: Component,
    composePanel: ComposePanel,
    session: AppSession? = null,
    initialState: AppUiState = AppUiState(composeEnabled = true, loading = LoadingUiState()),
    onVisibilityChanged: (Boolean) -> Unit = { composePanel.isVisible = it },
    onStateChanged: (AppUiState) -> Unit = {},
): DesktopComposeOverlay {
    check(SwingUtilities.isEventDispatchThread()) { "Install the Compose overlay on the EDT" }
    return DesktopComposeOverlay(initialState, { onDispose ->
        createDesktopComposeHost(owner, fallbackFocus, composePanel, onVisibilityChanged, onDispose)
    }, onStateChanged = onStateChanged).also { overlay -> session?.let(overlay::attach) }
}
