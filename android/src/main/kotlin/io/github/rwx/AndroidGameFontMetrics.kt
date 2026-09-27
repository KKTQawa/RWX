package io.github.rwx

import android.graphics.Paint
import android.graphics.Typeface
import io.github.rwx.render.canvas.GameFontMetricsProvider

/** Application-scoped; does not retain an Activity or share mutable Paint across threads. */
internal object AndroidGameFontMetrics : GameFontMetricsProvider {
    private val paints = ThreadLocal.withInitial { Paint(Paint.ANTI_ALIAS_FLAG) }

    private fun paint(sizePts: Float, key: String?): Paint = paints.get().apply {
        textSize = sizePts.coerceAtLeast(1f)
        typeface = typeface(key)
    }

    internal fun typeface(key: String?): Typeface {
        val family = key?.substringBeforeLast(':')?.takeUnless { it == "default" }
        val style = key?.substringAfterLast(':')?.toIntOrNull()?.and(3) ?: Typeface.NORMAL
        return Typeface.create(family, style)
    }

    override fun lineHeight(sizePts: Float, key: String?): Float = paint(sizePts, key).fontSpacing

    override fun textWidth(text: String, sizePts: Float, key: String?): Float =
        paint(sizePts, key).measureText(text)
}
