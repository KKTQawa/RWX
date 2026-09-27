package io.github.rwx.slick

import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy

/** Acquires the existing canvas context only for a GPU operation, not for mod file parsing. */
internal class SlickGlContext(private val canvas: SlickAwtGLCanvas) {
    fun <T> execute(action: () -> T): T {
        if (canvas.isGlContextCurrent()) return action()
        var result: Result<T>? = null
        canvas.runInContext { result = runCatching(action) }
        return checkNotNull(result).getOrThrow()
    }
}

/** A context/lifetime adapter for Slick, not a renderer: every operation uses the live backend. */
internal fun contextBoundSlickGraphics(backend: GraphicsEngine, context: SlickGlContext): GraphicsEngine =
    Proxy.newProxyInstance(GraphicsEngine::class.java.classLoader, arrayOf(GraphicsEngine::class.java)) { _, method, args ->
        context.execute {
            val result = try {
                method.invoke(backend, *(args ?: emptyArray()))
            } catch (error: InvocationTargetException) {
                throw error.targetException
            }
            when (result) {
                is GraphicsEngine -> contextBoundSlickGraphics(result, context)
                is SlickTexture -> result.confineTo(context)
                else -> result
            }
        }
    } as GraphicsEngine
