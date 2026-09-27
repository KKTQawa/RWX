package io.github.rwx

import io.github.rwx.render.canvas.GameFontMetricsProvider
import java.awt.Font
import java.awt.font.FontRenderContext
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap

/** Uses the same bundled AWT font families as Slick, without requiring a GL context. */
class DesktopFontMetrics(private val storage: PlatformStorage) : GameFontMetricsProvider {
    private val fonts = ConcurrentHashMap<String, Font>()
    private val context = FontRenderContext(null, true, true)

    override fun lineHeight(sizePts: Float, key: String?): Float =
        font(sizePts, key, "").getLineMetrics("Ag", context).height

    override fun textWidth(text: String, sizePts: Float, key: String?): Float =
        if (text.isEmpty()) 0f else font(sizePts, key, text).getStringBounds(text, context).width.toFloat()

    private fun font(sizePts: Float, key: String?, text: String): Font {
        val path = if (text.any { it.code > 255 } || key?.contains("fallback", ignoreCase = true) == true) {
            "font/DroidSansFallback.ttf"
        } else "font/Roboto-Regular.ttf"
        val base = fonts.computeIfAbsent(path) {
            storage.readAssetBytes(path)?.let { Font.createFont(Font.TRUETYPE_FONT, ByteArrayInputStream(it)) }
                ?: Font(Font.SANS_SERIF, Font.PLAIN, 1)
        }
        return base.deriveFont(sizePts.takeIf { it.isFinite() && it > 0 } ?: 1f)
    }
}
