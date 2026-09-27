package io.github.rwx.render.frame

@JvmInline
value class GameCanvasTextureId(val value: String)

@JvmInline
value class GameCanvasRenderTargetId(val value: String)

data class GameCanvasArgbImage(
    val width: Int,
    val height: Int,
    val pixels: IntArray,
    val premultipliedAlpha: Boolean = false,
) {
    fun copyPixels(): GameCanvasArgbImage = copy(pixels = pixels.copyOf())
}

data class GameCanvasTextureRef(
    val id: GameCanvasTextureId,
    val width: Int,
    val height: Int,
    val hasAlpha: Boolean = false,
    val requiresOrderedAlpha: Boolean = false,
    val premultipliedAlpha: Boolean = false,
) {
    init {
        require(width >= 0) { "Texture width must be non-negative" }
        require(height >= 0) { "Texture height must be non-negative" }
    }

    val widthFloat: Float = width.toFloat()
    val heightFloat: Float = height.toFloat()
    val safeWidthFloat: Float = width.coerceAtLeast(1).toFloat()
    val safeHeightFloat: Float = height.coerceAtLeast(1).toFloat()
    val inverseSafeWidth: Float = 1f / safeWidthFloat
    val inverseSafeHeight: Float = 1f / safeHeightFloat
    val fullRect: GameCanvasRect = GameCanvasRect.fromSize(widthFloat, heightFloat)
}

data class GameCanvasRenderTargetRef(
    val id: GameCanvasRenderTargetId,
    val width: Int,
    val height: Int,
) {
    init {
        require(width >= 0) { "Render target width must be non-negative" }
        require(height >= 0) { "Render target height must be non-negative" }
    }
}
