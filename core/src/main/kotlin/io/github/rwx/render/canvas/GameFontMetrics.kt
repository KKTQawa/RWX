package io.github.rwx.render.canvas

/** Real platform font measurement, without depending on a renderer or GPU context. */
interface GameFontMetricsProvider {
    fun lineHeight(sizePts: Float, key: String?): Float
    fun textWidth(text: String, sizePts: Float, key: String?): Float
}

object GameFontMetrics {
    @Volatile private var provider: GameFontMetricsProvider? = null
    fun install(provider: GameFontMetricsProvider) { this.provider = provider }
    fun lineHeight(sizePts: Float, key: String? = null): Float =
        provider?.lineHeight(sizePts, key) ?: sizePts.coerceAtLeast(1f)
    fun textWidth(text: String, sizePts: Float, key: String? = null): Float =
        provider?.textWidth(text, sizePts, key) ?: (text.length * sizePts.coerceAtLeast(1f) * 0.6f)
}
