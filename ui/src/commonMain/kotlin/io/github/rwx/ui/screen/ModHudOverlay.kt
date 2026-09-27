package io.github.rwx.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.rwx.ui.model.ModHudUiState

/**
 * Mod HUD layers over the running game, composed in registry order inside one transparent
 * full-screen box. Each layer keeps its own composition state across snapshot updates as long as
 * it stays registered; the host decides what to do with input the layers leave unconsumed.
 */
@Composable
fun ModHudOverlay(state: ModHudUiState, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().testTag("mod-hud")) {
        state.layers.forEach { layer ->
            key(layer.id) {
                Box(Modifier.fillMaxSize().testTag("mod-hud-layer:${layer.id}")) { layer.content() }
            }
        }
    }
}
