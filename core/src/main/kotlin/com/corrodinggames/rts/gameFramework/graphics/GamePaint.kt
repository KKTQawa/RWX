package com.corrodinggames.rts.gameFramework.graphics

import com.corrodinggames.rts.gameFramework.GameEngine
import io.github.rwx.render.canvas.DisplacementEffect
import io.github.rwx.render.canvas.Paint
import io.github.rwx.render.canvas.Typeface

class GamePaint : Paint() {
    private var displacementEffect: DisplacementEffect? = null
    private var shaderProgram: ShaderProgram? = null
    private var hasFlag: Boolean = false
    private var locked: Boolean = false

    fun o() {
        locked = true
    }

    fun c(value: Float) {
        super.b(value)
    }

    override fun b(value: Float) {
        if (locked) {
            GameEngine.logColored("UniquePaint changed when locked down:")
            GameEngine.logColored("from:" + textSize() + " to: " + value)
            GameEngine.printStackTrace()
        }
        super.b(value)
    }

    override fun a(typeface: Typeface?): Typeface? {
        if (locked) {
            GameEngine.logColored("UniquePaint changed when locked down:")
            GameEngine.printStackTrace()
        }
        return super.a(typeface)
    }

    fun p(): Boolean = hasFlag

    fun hasLegacyFlag(): Boolean = p()

    override fun a(value: Boolean) {
        hasFlag = value
        super.a(value)
    }

    fun q(): DisplacementEffect? = displacementEffect

    fun displacementEffect(): DisplacementEffect? = q()

    fun a(effect: DisplacementEffect?) {
        if (displacementEffect !== effect) {
            displacementEffect = effect
            markBackendStateChanged()
        }
    }

    fun setDisplacementEffect(effect: DisplacementEffect?) = a(effect)

    fun shaderProgram(): ShaderProgram? = shaderProgram

    fun a(shaderProgram: ShaderProgram?) {
        if (this.shaderProgram !== shaderProgram) {
            this.shaderProgram = shaderProgram
            markBackendStateChanged()
        }
    }

    fun setShaderProgram(shaderProgram: ShaderProgram?) = a(shaderProgram)

    companion object {
        @JvmField
        val r: GamePaint = GamePaint().apply {
            setColor(-1)
            o()
        }

        @JvmStatic
        fun b(paint: Paint) {
            (paint as GamePaint).o()
        }
    }
}
