package io.github.rwx.slick

import io.github.rwx.app.GameFramePresenter
import io.github.rwx.render.frame.GameFrame

/** Slick presents on its own GL thread; this is not an executor for recorded command frames. */
class SlickFramePresenter(private val presentSnapshot: () -> Unit = {}) : GameFramePresenter {
    override fun present(frame: GameFrame) {
        require(frame.commands.isEmpty()) {
            "Direct Slick presentation cannot execute recorded GameCanvas commands"
        }
        presentSnapshot()
    }
}
