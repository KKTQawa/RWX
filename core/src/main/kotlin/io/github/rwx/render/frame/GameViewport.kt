package io.github.rwx.render.frame

/** Logical canvas pixels; native hosts can be unmeasured during startup. */
data class GameViewport(val width: Int, val height: Int) {
    init {
        require(width >= 0 && height >= 0) { "Viewport dimensions must be non-negative" }
    }
    val w: Int get() = width
    val h: Int get() = height
    fun resolved(): GameViewport = GameViewport(
        width.takeIf { it > 0 } ?: 1280,
        height.takeIf { it > 0 } ?: 720,
    )
}
