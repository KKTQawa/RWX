package io.github.rwx

import android.os.Looper
import android.view.Choreographer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import io.github.rwx.app.FrameScheduler

/** One main-thread tick per display frame, suspended with the owning Activity. */
internal class AndroidFrameScheduler(
    private val lifecycle: Lifecycle,
) : FrameScheduler, DefaultLifecycleObserver, AutoCloseable {
    private val choreographer = Choreographer.getInstance()
    private var tick: (() -> Unit)? = null
    private var pending: Choreographer.FrameCallback? = null
    private var closed = false

    init {
        checkMainThread()
        lifecycle.addObserver(this)
    }

    override fun start(tick: () -> Unit) {
        checkMainThread()
        if (closed || lifecycle.currentState == Lifecycle.State.DESTROYED) return
        cancelPending()
        this.tick = tick
        schedule()
    }

    override fun stop() {
        checkMainThread()
        tick = null
        cancelPending()
    }

    override fun onResume(owner: LifecycleOwner) = schedule()

    override fun onPause(owner: LifecycleOwner) = cancelPending()

    override fun onDestroy(owner: LifecycleOwner) = close()

    override fun close() {
        checkMainThread()
        if (closed) return
        closed = true
        stop()
        lifecycle.removeObserver(this)
    }

    private fun schedule() {
        if (closed || tick == null || pending != null ||
            !lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return
        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (pending !== this) return
                pending = null
                if (closed || !lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return
                tick?.invoke()
                // start/stop/close may have been called by the tick itself.
                schedule()
            }
        }
        pending = callback
        choreographer.postFrameCallback(callback)
    }

    private fun cancelPending() {
        pending?.let(choreographer::removeFrameCallback)
        pending = null
    }

    private fun checkMainThread() {
        check(Looper.myLooper() === Looper.getMainLooper()) { "Android frames must run on the main thread" }
    }
}
