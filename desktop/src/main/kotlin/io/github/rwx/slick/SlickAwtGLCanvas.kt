package io.github.rwx.slick

import org.lwjgl.opengl.*
import org.lwjgl.opengl.awt.AWTGLCanvas
import org.lwjgl.opengl.awt.GLData
import org.lwjgl.opengl.awt.PlatformLinuxGLCanvas
import org.lwjgl.system.Platform
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent

internal class SlickAwtGLCanvas(
    data: GLData,
    private var requestedSwapInterval: Int? = null,
) : AWTGLCanvas(data) {
    private var appliedSwapInterval: Int? = null

    @Volatile
    private var drawingSurfaceInitialized = false

    internal val hasLiveDrawingSurface: Boolean get() = drawingSurfaceInitialized

    override fun beforeRender() {
        super.beforeRender()
        drawingSurfaceInitialized = true
    }

    override fun disposeCanvas() {
        if (!drawingSurfaceInitialized) return
        super.disposeCanvas()
        drawingSurfaceInitialized = false
    }

    internal fun isGlContextCurrent(): Boolean = context != 0L && platformCanvas.isCurrent(context)

    override fun initGL() = Unit

    override fun paintGL() = Unit

    override fun processMouseEvent(event: MouseEvent) {
        if (!SlickCanvasHost.dispatchOverlayInput(event)) super.processMouseEvent(event)
    }

    override fun processMouseMotionEvent(event: MouseEvent) {
        if (!SlickCanvasHost.dispatchOverlayInput(event)) super.processMouseMotionEvent(event)
    }

    override fun processMouseWheelEvent(event: MouseWheelEvent) {
        if (!SlickCanvasHost.dispatchOverlayInput(event)) super.processMouseWheelEvent(event)
    }

    fun requestSwapInterval(interval: Int?) {
        requestedSwapInterval = interval
    }

    fun applyRuntimeGlSettings() {
        val interval = requestedSwapInterval ?: return
        if (appliedSwapInterval == interval) return

        val applied = runCatching {
            when (Platform.get()) {
                Platform.LINUX -> applyLinuxSwapInterval(interval)
                Platform.WINDOWS -> applyWindowsSwapInterval(interval)
                Platform.MACOSX -> applyMacSwapInterval(interval)
                else -> false
            }
        }.getOrDefault(false)

        if (applied) {
            appliedSwapInterval = interval
        }
    }

    private fun applyLinuxSwapInterval(interval: Int): Boolean {
        val capabilities = GL.getCapabilitiesGLX()
        if (!capabilities.GLX_EXT_swap_control) return false

        val linuxCanvas = platformCanvas as? PlatformLinuxGLCanvas
        val display = linuxCanvas?.display?.takeIf { it != 0L } ?: GLX12.glXGetCurrentDisplay()
        val drawable = linuxCanvas?.drawable?.takeIf { it != 0L } ?: GLX.glXGetCurrentDrawable()
        if (display == 0L || drawable == 0L) return false

        GLXEXTSwapControl.glXSwapIntervalEXT(display, drawable, interval)
        return true
    }

    private fun applyWindowsSwapInterval(interval: Int): Boolean {
        val capabilities = GL.getCapabilitiesWGL()
        return capabilities.WGL_EXT_swap_control && WGLEXTSwapControl.wglSwapIntervalEXT(interval)
    }

    private fun applyMacSwapInterval(interval: Int): Boolean {
        val context = CGL.CGLGetCurrentContext()
        return context != 0L && CGL.CGLSetParameter(context, CGL.kCGLCPSwapInterval, interval) == 0
    }
}
