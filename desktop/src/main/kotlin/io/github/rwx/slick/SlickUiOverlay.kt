package io.github.rwx.slick

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import org.newdawn.slick.Graphics
import java.nio.IntBuffer

/** Premultiplied UI pixels only. Game frames always stay on the native Slick drawable. */
internal class SlickUiFrame(val width: Int, val height: Int, val pixels: IntArray)

internal class SlickUiOverlay {
    private var texture = 0
    private var uploaded: SlickUiFrame? = null
    private var uploadBuffer: IntBuffer? = null

    fun render(frame: SlickUiFrame?, width: Int, height: Int, graphics: Graphics) {
        if (frame == null) return
        Graphics.setCurrent(graphics)
        graphics.flushBuffer()
        val program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM)
        val activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE)
        val matrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE)
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
        GL11.glPushClientAttrib(GL11.GL_CLIENT_PIXEL_STORE_BIT)
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glPushMatrix()
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GL11.glPushMatrix()
        try {
            GL20.glUseProgram(0)
            GL13.glActiveTexture(GL13.GL_TEXTURE0)
            if (texture == 0) texture = GL11.glGenTextures()
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture)
            if (uploaded !== frame) {
                GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4)
                GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0)
                GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0)
                GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0)
                val buffer = uploadBuffer?.takeIf { it.capacity() >= frame.pixels.size }
                    ?: BufferUtils.createIntBuffer(frame.pixels.size).also { uploadBuffer = it }
                buffer.clear()
                buffer.put(frame.pixels).flip()
                if (uploaded?.width != frame.width || uploaded?.height != frame.height) {
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, frame.width, frame.height, 0,
                        GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer)
                } else {
                    GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, frame.width, frame.height,
                        GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer)
                }
                uploaded = frame
            }
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_STENCIL_TEST)
            GL11.glDisable(GL11.GL_SCISSOR_TEST)
            GL11.glDisable(GL11.GL_ALPHA_TEST)
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glColorMask(true, true, true, true)
            GL11.glColor4f(1f, 1f, 1f, 1f)
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glLoadIdentity()
            GL11.glOrtho(0.0, width.toDouble(), height.toDouble(), 0.0, -1.0, 1.0)
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glLoadIdentity()
            GL11.glBegin(GL11.GL_QUADS)
            GL11.glTexCoord2f(0f, 0f); GL11.glVertex2f(0f, 0f)
            GL11.glTexCoord2f(1f, 0f); GL11.glVertex2f(width.toFloat(), 0f)
            GL11.glTexCoord2f(1f, 1f); GL11.glVertex2f(width.toFloat(), height.toFloat())
            GL11.glTexCoord2f(0f, 1f); GL11.glVertex2f(0f, height.toFloat())
            GL11.glEnd()
        } finally {
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPopMatrix()
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPopMatrix()
            GL11.glMatrixMode(matrixMode)
            GL11.glPopClientAttrib()
            GL11.glPopAttrib()
            GL13.glActiveTexture(activeTexture)
            GL20.glUseProgram(program)
        }
    }

    fun dispose() {
        if (texture != 0) GL11.glDeleteTextures(texture)
        texture = 0
        uploaded = null
        uploadBuffer = null
    }
}
