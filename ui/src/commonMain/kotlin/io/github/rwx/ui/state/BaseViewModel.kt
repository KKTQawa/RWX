package io.github.rwx.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Stage 2: ViewModel base for UI state management.
 * Provides reactive state flow from :core game logic to Compose UI.
 */
abstract class BaseViewModel {
    /**
     * Called when ViewModel is first created.
     */
    open fun onCreated() {}

    /**
     * Called when ViewModel is being disposed.
     */
    open fun onDisposed() {}
}

/**
 * Remember a ViewModel instance across recompositions.
 */
@Composable
inline fun <reified T : BaseViewModel> rememberViewModel(
    crossinline factory: () -> T
): T {
    return remember {
        factory().apply { onCreated() }
    }
}
