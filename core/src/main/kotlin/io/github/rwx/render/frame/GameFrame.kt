package io.github.rwx.render.frame

data class GameCanvasState(
    val transform: GameCanvasTransform = GameCanvasTransform.Identity,
    val clip: GameCanvasRect? = null,
    val renderTarget: GameCanvasRenderTargetId? = null,
) {
    companion object {
        val Default: GameCanvasState = GameCanvasState()
    }
}

sealed interface GameCanvasCommand {
    data class Clear(
        val color: GameCanvasColor,
        val blendMode: GameCanvasBlendMode = GameCanvasBlendMode.SourceOver,
        val renderTarget: GameCanvasRenderTargetId? = null,
    ) : GameCanvasCommand

    data class DrawTexture(
        val texture: GameCanvasTextureRef,
        val source: GameCanvasRect,
        val destination: GameCanvasRect,
        val paint: GameCanvasPaint,
        val state: GameCanvasState,
    ) : GameCanvasCommand {
        val sourceIsFullTexture: Boolean =
            source.left == 0f &&
                    source.top == 0f &&
                    source.right == texture.widthFloat &&
                    source.bottom == texture.heightFloat
        val sourceU0: Float = if (sourceIsFullTexture) 0f else source.left * texture.inverseSafeWidth
        val sourceV0: Float = if (sourceIsFullTexture) 0f else source.top * texture.inverseSafeHeight
        val sourceU1: Float = if (sourceIsFullTexture) 1f else source.right * texture.inverseSafeWidth
        val sourceV1: Float = if (sourceIsFullTexture) 1f else source.bottom * texture.inverseSafeHeight
    }

    data class DrawRect(
        val rect: GameCanvasRect,
        val paint: GameCanvasPaint,
        val state: GameCanvasState,
    ) : GameCanvasCommand

    data class DrawLine(
        val start: GameCanvasPoint,
        val end: GameCanvasPoint,
        val paint: GameCanvasPaint,
        val state: GameCanvasState,
    ) : GameCanvasCommand

    data class DrawCircle(
        val center: GameCanvasPoint,
        val radius: Float,
        val paint: GameCanvasPaint,
        val state: GameCanvasState,
    ) : GameCanvasCommand {
        init {
            require(radius >= 0f) { "Circle radius must be non-negative" }
        }
    }

    data class DrawText(
        val text: String,
        val baseline: GameCanvasPoint,
        val paint: GameCanvasPaint,
        val state: GameCanvasState,
    ) : GameCanvasCommand
}

data class GameFrame(
    val viewport: GameViewport,
    val commands: List<GameCanvasCommand>,
)
