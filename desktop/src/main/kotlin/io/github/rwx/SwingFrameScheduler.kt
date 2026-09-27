package io.github.rwx

import io.github.rwx.app.FrameScheduler
import javax.swing.SwingUtilities
import javax.swing.Timer

/** A single coalescing EDT timer; stop also cancels callbacks already queued by the timer. */
class SwingFrameScheduler(
    private val delayMillis: Int = 16,
    private val framePump: () -> Unit = {},
) : FrameScheduler {
    init { require(delayMillis > 0) }
    private var timer: Timer? = null

    override fun start(tick: () -> Unit) = onEdt {
        timer?.stop()
        val next = Timer(delayMillis, null)
        next.isCoalesce = true
        next.addActionListener {
            if (timer === next) {
                try {
                    framePump()
                } catch (_: Throwable) {
                }
                tick()
            }
        }
        timer = next
        next.start()
    }

    override fun stop() = onEdt {
        val previous = timer
        timer = null
        previous?.stop()
    }

    private fun onEdt(action: () -> Unit) {
        if (SwingUtilities.isEventDispatchThread()) action() else SwingUtilities.invokeAndWait(action)
    }
}
