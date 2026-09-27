package io.github.rwx.slick

import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.swing.JPanel
import javax.swing.SwingUtilities

/** CPU copy for Compose backgrounds; never registers pixels with a GPU texture registry. */
internal class SlickSnapshotPanel : JPanel() {
    private var snapshot: SlickFrameSnapshot? = null
    private var image: BufferedImage? = null

    init { background = Color.BLACK; isOpaque = true }

    fun present(frame: SlickFrameSnapshot?) {
        check(SwingUtilities.isEventDispatchThread())
        if (frame === snapshot) return
        snapshot = frame
        image = frame?.let {
            require(it.width > 0 && it.height > 0 && it.pixels.size >= it.width.toLong() * it.height)
            BufferedImage(it.width, it.height, BufferedImage.TYPE_INT_ARGB).apply {
                setRGB(0, 0, it.width, it.height, it.pixels, 0, it.width)
            }
        }
        repaint()
    }

    override fun paintComponent(graphics: Graphics) {
        super.paintComponent(graphics)
        image?.let { graphics.drawImage(it, 0, 0, width, height, null) }
    }
}
