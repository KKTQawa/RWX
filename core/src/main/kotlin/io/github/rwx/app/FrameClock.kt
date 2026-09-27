package io.github.rwx.app

fun interface FrameClock {
    fun deltaSeconds(): Float
}

/** Monotonic elapsed time, bounded after pauses/debugger stops. */
class SystemNanoFrameClock(
    private val clampMax: Float = 0.1f,
    private val nanoTime: () -> Long = System::nanoTime,
) : FrameClock {
    init { require(clampMax.isFinite() && clampMax > 0f) }
    private var last = nanoTime()
    override fun deltaSeconds(): Float {
        val now = nanoTime()
        val delta = (now - last) / 1_000_000_000f
        last = now
        return delta.coerceIn(0f, clampMax)
    }
}
