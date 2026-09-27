package com.corrodinggames.rts.gameFramework;

import com.corrodinggames.rts.gameFramework.graphics.GamePaint;
import io.github.rwx.render.canvas.Paint;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.m */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/m.class */
class PaintSizeTracker {

    /* JADX INFO: renamed from: a */
    float textSize;

    /* JADX INFO: renamed from: b */
    Paint paint;

    final /* synthetic */ GameEngine c;

    PaintSizeTracker(GameEngine gameEngine) {
        this.c = gameEngine;
    }

    void a() {
        float screenPixels = this.c.toScreenPixels(this.textSize);
        if (this.paint.k() != screenPixels) {
            if (this.paint instanceof GamePaint) {
                ((GamePaint) this.paint).c(screenPixels);
            } else {
                this.paint.b(screenPixels);
            }
        }
    }
}
