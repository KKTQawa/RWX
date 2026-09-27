package io.github.rwx.slick

import java.awt.Canvas
import java.awt.Dimension
import java.awt.event.InputEvent
import javax.swing.SwingUtilities

object SlickCanvasHost {
    @Volatile
    private var uiFrameProvider: (() -> SlickUiFrame?)? = null

    private var uiInputDispatcher: ((InputEvent) -> Boolean)? = null

    internal fun installUiOverlay(frameProvider: () -> SlickUiFrame?, inputDispatcher: (InputEvent) -> Boolean) {
        uiFrameProvider = frameProvider
        uiInputDispatcher = inputDispatcher
    }

    internal fun currentUiFrame(): SlickUiFrame? = uiFrameProvider?.invoke()

    internal fun dispatchOverlayInput(event: InputEvent): Boolean = uiInputDispatcher?.invoke(event) == true

    @Volatile
    private var canvasProvider: (() -> Canvas?)? = null

    @Volatile
    private var visibilityController: ((Boolean, Boolean) -> Unit)? = null

    @Volatile
    private var resizeController: ((Int, Int) -> Unit)? = null

    @Volatile
    private var rendererShutdown: (() -> Unit)? = null

    fun install(
        canvasProvider: () -> Canvas?,
        visibilityController: (Boolean, Boolean) -> Unit,
    ) {
        this.canvasProvider = canvasProvider
        this.visibilityController = visibilityController
    }

    /** Detach only this host; a late close must not unregister a replacement window. */
    fun uninstall(canvas: Canvas) {
        check(SwingUtilities.isEventDispatchThread())
        if (canvasProvider?.invoke() !== canvas) return
        uiFrameProvider = null
        uiInputDispatcher = null
        canvasProvider = null
        visibilityController = null
        resizeController = null
        rendererShutdown = null
    }

    fun setRendererShutdown(shutdown: (() -> Unit)?) {
        rendererShutdown = shutdown
    }

    /**
     * Stops the embedded renderer and blocks until its GL thread has released the AWT drawing
     * surface. Call before disposing the game canvas' window to avoid a native JAWT crash.
     */
    fun shutdownRenderer() {
        rendererShutdown?.invoke()
    }

    fun gameCanvas(): Canvas? = canvasProvider?.invoke()

    fun gameCanvasSize(): Dimension? = canvasProvider?.invoke()?.size

    fun setResizeController(controller: ((Int, Int) -> Unit)?) {
        resizeController = controller
    }

    fun notifyGameCanvasResized(width: Int, height: Int) {
        resizeController?.invoke(width, height)
    }

    fun setGameVisible(visible: Boolean, overlay: Boolean = false) {
        visibilityController?.invoke(visible, overlay)
    }

    fun requestGameFocus() {
        val canvas = gameCanvas() ?: return
        val action = {
            if (canvas.isVisible && canvas.isShowing) {
                canvas.isFocusable = true
                if (!canvas.requestFocusInWindow()) {
                    canvas.requestFocus()
                }
            }
        }
        if (SwingUtilities.isEventDispatchThread()) {
            action()
        } else {
            SwingUtilities.invokeLater(action)
        }
    }
}
