package io.github.rwx.render.canvas

import io.github.rwx.render.frame.*

open class ColorFilter

class BlendColorFilter(
    @JvmField val color: Int,
    @JvmField val blendMode: GameCanvasBlendMode,
) : ColorFilter()

class MultiplyAddColorFilter(
    @JvmField val multiplyColor: Int,
    @JvmField val addColor: Int,
) : ColorFilter() {
    fun usesLegacyAdditiveBlend(): Boolean =
        multiplyColor != 0 && multiplyColor != -1
}
