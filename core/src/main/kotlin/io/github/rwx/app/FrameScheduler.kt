package io.github.rwx.app

/** Platform-owned UI-thread ticks. Starting again replaces the previous callback. */
interface FrameScheduler {
    fun start(tick: () -> Unit)
    fun stop()
}

/** Guards even callbacks already queued before a stop/restart. Confined to the platform UI thread. */
internal class OwnedFrameLoop(
    private val scheduler: FrameScheduler,
    private val clock: FrameClock,
    private val drive: (Float) -> Unit,
) : AutoCloseable {
    private var generation = 0L
    fun start() {
        val current = ++generation
        scheduler.start {
            if (generation == current) drive(clock.deltaSeconds())
        }
    }
    override fun close() {
        generation++
        scheduler.stop()
    }
}
