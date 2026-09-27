package io.github.rwx.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.itemAppear(enabled: Boolean): Modifier = composed {
    if (!enabled) return@composed this
    val state = remember { MutableTransitionState(false).apply { targetState = true } }
    val transition = updateTransition(state, label = "itemAppear")
    val alpha by transition.animateFloat(transitionSpec = { tween(300) }) { visible -> if (visible) 1f else 0f }
    val scale by transition.animateFloat(transitionSpec = { tween(300, easing = FastOutSlowInEasing) }) { visible ->
        if (visible) 1f else 0.96f
    }
    this.graphicsLayer {
        this.alpha = alpha
        scaleX = scale
        scaleY = scale
    }
}

fun <T> List<T>.withStableKeys(idOf: (T) -> String): List<String> {
    val occurrences = mutableMapOf<String, Int>()
    return map { item ->
        val id = idOf(item)
        val occurrence = occurrences.getOrDefault(id, 0)
        occurrences[id] = occurrence + 1
        "$id#$occurrence"
    }
}
