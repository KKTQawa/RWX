package io.github.rwx.app

import io.github.rwx.render.frame.GameFrame

fun interface GameFramePresenter {
    fun present(frame: GameFrame)
}
