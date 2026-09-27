package io.github.rwx.render.frame


enum class GameCanvasPaintStyle {
    Fill,
    Stroke,
    FillAndStroke,
}

enum class GameCanvasTextureFilter {
    Nearest,
    Linear,
}

enum class GameCanvasBlendMode {
    SourceOver,
    Clear,
    ClearAlpha,
    Source,
    Destination,
    SourceIn,
    SourceOut,
    SourceAtop,
    DestinationOver,
    DestinationIn,
    DestinationOut,
    DestinationAtop,
    Xor,
    Add,
    Multiply,
    Screen,
    Overlay,
}

enum class GameCanvasTextAlign {
    Left,
    Center,
    Right,
}

data class GameCanvasColor(
    val argb: Int,
) {
    val alpha: Int get() = argb ushr 24
    val red: Int get() = (argb ushr 16) and 0xff
    val green: Int get() = (argb ushr 8) and 0xff
    val blue: Int get() = argb and 0xff

    companion object {
        val Transparent: GameCanvasColor = GameCanvasColor(0x00000000)
        val White: GameCanvasColor = GameCanvasColor(-0x1)
    }
}

enum class GameCanvasTeamColorMode {
    PureGreen,
    HueAdd,
    HueShift,
}

sealed interface GameCanvasTextureEffect {
    data class TeamColor(
        val mode: GameCanvasTeamColorMode,
        val color: GameCanvasColor,
        val amount: Float = 1f,
    ) : GameCanvasTextureEffect

    data class Displacement(
        val screenBase: GameCanvasTextureRef,
        val offsetBy: Float,
    ) : GameCanvasTextureEffect
}

data class GameCanvasPaint(
    val color: GameCanvasColor = GameCanvasColor.White,
    val alphaMultiplier: Float = 1f,
    val style: GameCanvasPaintStyle = GameCanvasPaintStyle.Fill,
    val strokeWidth: Float = 1f,
    val textureFilter: GameCanvasTextureFilter = GameCanvasTextureFilter.Linear,
    val blendMode: GameCanvasBlendMode = GameCanvasBlendMode.SourceOver,
    val textureEffect: GameCanvasTextureEffect? = null,
    val textSize: Float = 16f,
    val textAlign: GameCanvasTextAlign = GameCanvasTextAlign.Left,
    val typefaceKey: String? = null,
) {
    init {
        require(alphaMultiplier in 0f..1f) { "Alpha multiplier must be in 0..1" }
        require(strokeWidth >= 0f) { "Stroke width must be non-negative" }
        require(textSize >= 0f) { "Text size must be non-negative" }
    }

    companion object {
        val Default: GameCanvasPaint = GameCanvasPaint()
        val DefaultNearest: GameCanvasPaint = GameCanvasPaint(textureFilter = GameCanvasTextureFilter.Nearest)
    }
}
