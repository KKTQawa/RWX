package com.corrodinggames.rts.gameFramework.ui.widgets;

import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import io.github.rwx.geometry.Rect;
import io.github.rwx.render.canvas.Paint;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.a.e */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/a/e.class */
public class NinePatchStyle extends UIStyle {
    /* JADX INFO: renamed from: a */
    int patchWidth;

    /* JADX INFO: renamed from: b */
    int patchHeight;

    /* JADX INFO: renamed from: c */
    float normalizedWidth;

    /* JADX INFO: renamed from: d */
    float normalizedHeight;
    /* JADX INFO: renamed from: e */
    public boolean scalePatchToFit = true;
    /* JADX INFO: renamed from: f */
    public boolean useScaledBlit = false;
    /* JADX INFO: renamed from: g */
    public float patchScale = 1.0f;
    private static final int TILE_IMAGE_LIMIT = 2000;
    /* JADX INFO: renamed from: h */
    static Rect stretchCheckRect = new Rect();
    /* JADX INFO: renamed from: i */
    static Rect halfSizeRect = new Rect();

    public NinePatchStyle() {
    }

    public NinePatchStyle(Texture texture, int i2, int i3) {
        setBackgroundTexture(texture);
        setPatchSize(texture, i2, i3);
    }

    /* JADX INFO: renamed from: a */
    public void setPatchSize(Texture texture, int i2, int i3) {
        this.patchWidth = i2;
        this.patchHeight = i3;
        this.normalizedWidth = i2 / (float) texture.p;
        this.normalizedHeight = i3 / (float) texture.q;
    }

    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public NinePatchStyle clone() {
        NinePatchStyle ninePatchStyle = new NinePatchStyle();
        ninePatchStyle.copyFrom(this);
        return ninePatchStyle;
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.widgets.UIStyle
    /* JADX INFO: renamed from: a */
    public void copyFrom(UIStyle uIStyle) {
        NinePatchStyle ninePatchStyle = (NinePatchStyle) uIStyle;
        this.patchWidth = ninePatchStyle.patchWidth;
        this.patchHeight = ninePatchStyle.patchHeight;
        this.normalizedWidth = ninePatchStyle.normalizedWidth;
        this.normalizedHeight = ninePatchStyle.normalizedHeight;
        this.scalePatchToFit = ninePatchStyle.scalePatchToFit;
        super.copyFrom(ninePatchStyle);
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.widgets.UIStyle
    /* JADX INFO: renamed from: a */
    public void setBackgroundTexture(Texture texture) {
        super.setBackgroundTexture(texture);
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.widgets.UIStyle
    /* JADX INFO: renamed from: a */
    public void drawBackground(GraphicsEngine graphicsEngine, Rect rect) {
        drawNinePatch(graphicsEngine, rect);
        if (this.borderPaint != null) {
        }
    }

    /* JADX INFO: renamed from: b */
    public void drawNinePatch(GraphicsEngine graphicsEngine, Rect rect) {
        drawPatch(graphicsEngine, this.backgroundTexture, this.backgroundPaint, rect);
    }

    /* JADX INFO: renamed from: c */
    private boolean supportsPartialPatches() {
        return true;
    }

    private void drawPatch(GraphicsEngine graphicsEngine, Texture texture, Paint paint, Rect rect) {
        int i2 = rect.a;
        int i3 = rect.b;
        int iB = rect.b();
        int iC = rect.c();
        int i4 = this.patchWidth;
        int i5 = this.patchHeight;
        if (!this.scalePatchToFit) {
            if (i4 > iB / 2) {
                i4 = iB / 2;
            }
            if (i5 > iC / 2) {
                i5 = iC / 2;
            }
        } else {
            float f = 1.0f;
            int i6 = iB / 2;
            int i7 = iC / 2;
            if (i4 * 1.0f > i6) {
                f = i6 / (float) i4;
            }
            if (i5 * f > i7) {
                f = i7 / (float) i5;
            }
            i4 = (int) (this.patchWidth * f);
            i5 = (int) (this.patchHeight * f);
        }
        int i8 = iB - (2 * i4);
        int i9 = iC - (2 * i5);
        float f2 = this.normalizedWidth;
        float f3 = this.normalizedHeight;
        if (supportsPartialPatches()) {
            drawPatchRegion(graphicsEngine, texture, paint, i2 + i4, i3 + 0, i8, i5, f2, 0.0f, 1.0f - f2, f3, this.useScaledBlit);
            drawPatchRegion(graphicsEngine, texture, paint, i2 + 0, i3 + i5, i4, i9, 0.0f, f3, f2, 1.0f - f3, this.useScaledBlit);
            drawPatchRegion(graphicsEngine, texture, paint, i2 + i4, (i3 + iC) - i5, i8, i5, f2, 1.0f - f3, 1.0f - f2, 1.0f, this.useScaledBlit);
            drawPatchRegion(graphicsEngine, texture, paint, (i2 + iB) - i4, i3 + i5, i4, i9, 1.0f - f2, f3, 1.0f, 1.0f - f3, this.useScaledBlit);
            drawPatchRegion(graphicsEngine, texture, paint, i2 + 0, i3 + 0, i4, i5, 0.0f, 0.0f, this.normalizedWidth, this.normalizedHeight);
            drawPatchRegion(graphicsEngine, texture, paint, (i2 + iB) - i4, i3 + 0, i4, i5, 1.0f - this.normalizedWidth, 0.0f, 1.0f, this.normalizedHeight);
            drawPatchRegion(graphicsEngine, texture, paint, i2 + 0, (i3 + iC) - i5, i4, i5, 0.0f, 1.0f - this.normalizedHeight, this.normalizedWidth, 1.0f);
            drawPatchRegion(graphicsEngine, texture, paint, (i2 + iB) - i4, (i3 + iC) - i5, i4, i5, 1.0f - this.normalizedWidth, 1.0f - this.normalizedHeight, 1.0f, 1.0f);
        }
        drawPatchRegion(graphicsEngine, texture, paint, i2 + i4, i3 + i5, i8, i9, f2, f3, 1.0f - f2, 1.0f - f3, this.useScaledBlit);
    }

    public void drawPatchRegion(GraphicsEngine graphicsEngine, Texture texture, Paint paint, int i2, int i3, int i4, int i5, float f, float f2, float f3, float f4) {
        drawPatchRegion(graphicsEngine, texture, paint, i2, i3, i4, i5, f, f2, f3, f4, false);
    }

    public void drawPatchRegion(GraphicsEngine graphicsEngine, Texture texture, Paint paint, int i2, int i3, int i4, int i5, float f, float f2, float f3, float f4, boolean z) {
        Rect rect = stretchCheckRect;
        Rect rect2 = halfSizeRect;
        rect.a((int) (f * texture.p), (int) (f2 * texture.q), (int) (f3 * texture.p), (int) (f4 * texture.q));
        rect2.a(i2, i3, i2 + i4, i3 + i5);
        if (!z) {
            graphicsEngine.a(texture, rect, rect2, paint);
        } else {
            drawRepeatedSubRect(graphicsEngine, texture, new Rect(rect), new Rect(rect2), paint, this.patchScale);
        }
    }

    private static void drawRepeatedSubRect(GraphicsEngine graphicsEngine, Texture texture, Rect source, Rect destination, Paint paint, float scaleBias) {
        int sourceWidth = source.b();
        int sourceHeight = source.c();
        int tileLeft = destination.a;
        int tileTop = destination.b;
        int drawWidth = destination.c - tileLeft;
        int drawHeight = destination.d - tileTop;
        if (sourceWidth == 0 || sourceHeight == 0) {
            return;
        }
        int tileCountX = (int) ((drawWidth / (float) sourceWidth) + 0.5f);
        int tileCountY = (int) ((drawHeight / (float) sourceHeight) + 0.5f);
        if (tileCountX < 1) {
            tileCountX = 1;
        }
        if (tileCountY < 1) {
            tileCountY = 1;
        }
        if (tileCountX == 0 || tileCountY == 0) {
            return;
        }

        float tileScaleX = drawWidth / (float) (tileCountX * sourceWidth);
        float tileScaleY = drawHeight / (float) (tileCountY * sourceHeight);
        float adjustedScaleX = Utility.lerp(1.0f, tileScaleX, scaleBias);
        float adjustedScaleY = Utility.lerp(1.0f, tileScaleY, scaleBias);
        if (Math.abs(adjustedScaleX) < 0.0001f) {
            adjustedScaleX = 1.0f;
        }
        if (Math.abs(adjustedScaleY) < 0.0001f) {
            adjustedScaleY = 1.0f;
        }

        int tileWidth = (int) (sourceWidth * adjustedScaleX);
        int tileHeight = (int) (sourceHeight * adjustedScaleY);
        float sourceScaleX = 1.0f / adjustedScaleX;
        float sourceScaleY = 1.0f / adjustedScaleY;
        if (tileWidth <= 0 || tileHeight <= 0) {
            return;
        }

        int tileCount = 0;
        int repeatTop = tileTop;
        while (tileLeft < destination.c) {
            while (tileTop < destination.d) {
                tileCount++;
                if (tileCount > TILE_IMAGE_LIMIT) {
                    GameEngine.log("tileImage hit limit");
                    return;
                }
                int clippedTileWidth = destination.c - tileLeft;
                if (clippedTileWidth > tileWidth) {
                    clippedTileWidth = tileWidth;
                }
                int clippedTileHeight = destination.d - tileTop;
                if (clippedTileHeight > tileHeight) {
                    clippedTileHeight = tileHeight;
                }
                if (clippedTileHeight > 0 && clippedTileWidth > 0) {
                    stretchCheckRect.a(0, 0, (int) (clippedTileWidth * sourceScaleX), (int) (clippedTileHeight * sourceScaleY));
                    stretchCheckRect.a(source.a, source.b);
                    halfSizeRect.a(tileLeft, tileTop, tileLeft + clippedTileWidth, tileTop + clippedTileHeight);
                    int clipLeftDelta = halfSizeRect.a - destination.a;
                    if (clipLeftDelta < 0) {
                        stretchCheckRect.a -= clipLeftDelta;
                        halfSizeRect.a -= clipLeftDelta;
                    }
                    int clipTopDelta = halfSizeRect.b - destination.b;
                    if (clipTopDelta < 0) {
                        stretchCheckRect.b -= clipTopDelta;
                        halfSizeRect.b -= clipTopDelta;
                    }
                    graphicsEngine.a(texture, stretchCheckRect, halfSizeRect, paint);
                    tileTop += tileHeight;
                }
            }
            tileLeft += tileWidth;
            tileTop = repeatTop;
        }
    }
}
