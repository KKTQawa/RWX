package io.github.rwx.ui

/** Immutable, engine-neutral sRGB channels. Hex values use RGBA order, not Android ARGB. */
data class UiColor(val r: Float, val g: Float, val b: Float, val a: Float = 1f) {
    constructor(rgbaHex: String) : this(parseRgba(rgbaHex))

    private constructor(rgba: Long) : this(
        ((rgba shr 24) and 0xff) / 255f,
        ((rgba shr 16) and 0xff) / 255f,
        ((rgba shr 8) and 0xff) / 255f,
        (rgba and 0xff) / 255f,
    )

    fun withAlpha(alpha: Float): UiColor = copy(a = alpha)

    fun mix(other: UiColor, weight: Float): UiColor = UiColor(
        r + (other.r - r) * weight,
        g + (other.g - g) * weight,
        b + (other.b - b) * weight,
        a + (other.a - a) * weight,
    )

    companion object {
        val BLACK = UiColor(0f, 0f, 0f)
        val WHITE = UiColor(1f, 1f, 1f)

        private fun parseRgba(hex: String): Long {
            require(hex.length == 8 && hex.all { it.digitToIntOrNull(16) != null }) {
                "Expected eight RGBA hexadecimal digits"
            }
            return hex.toLong(16)
        }
    }
}
