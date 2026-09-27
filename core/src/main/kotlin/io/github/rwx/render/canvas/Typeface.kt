package io.github.rwx.render.canvas

class Typeface private constructor(
    private val nativeHandle: Int,
) {
    private var style: Int = defaultStyle(nativeHandle)
    private var family: String? = null

    fun a(): Boolean = (style and 1) != 0

    val key: String
        get() = (family ?: "default") + ":" + style

    override fun equals(other: Any?): Boolean =
        this === other || (other is Typeface && style == other.style && nativeHandle == other.nativeHandle)

    override fun hashCode(): Int = (31 * nativeHandle) + style

    @Suppress("deprecation")
    protected fun finalize() {
        release(nativeHandle)
    }

    companion object {
        private val cache = mutableMapOf<Int, MutableMap<Int, Typeface>>()

        @JvmField
        val a: Typeface = a(null as String?, 0)

        @JvmField
        val b: Typeface = a(null as String?, 1)

        @JvmField
        val c: Typeface = a("sans-serif", 0)

        @JvmField
        val d: Typeface = a("serif", 0)

        @JvmField
        val e: Typeface = a("monospace", 0)

        @JvmField
        val f: Array<Typeface> = arrayOf(a, b, a(null as String?, 2), a(null as String?, 3))

        @JvmStatic
        fun a(family: String?, style: Int): Typeface =
            Typeface(0).apply {
                this.style = style
                this.family = family
            }

        @JvmStatic
        fun a(typeface: Typeface?, style: Int): Typeface {
            var nativeHandle = 0
            if (typeface != null) {
                if (typeface.style == style) {
                    return typeface
                }
                nativeHandle = typeface.nativeHandle
            }
            val typefaceCache = cache.getOrPut(nativeHandle) { HashMap(4) }
            typefaceCache[style]?.let { return it }

            return Typeface(0).apply {
                this.style = style
                this.family = typeface?.family
                typefaceCache[style] = this
            }
        }

        private fun release(nativeHandle: Int) = Unit

        private fun defaultStyle(nativeHandle: Int): Int = 0
    }
}
