package com.corrodinggames.rts.gameFramework.graphics;

import io.github.rwx.render.canvas.ArgbColor;

public final class TeamColoring {
    private TeamColoring() {
    }

    public static int pureGreen(int color, int teamColor) {
        int alpha = ArgbColor.a(color);
        if (alpha == 0) {
            return color == 0 ? color : 0;
        }
        int green = ArgbColor.c(color);
        int red = ArgbColor.b(color);
        if (green <= 0 || red != ArgbColor.d(color)) {
            return color;
        }
        int teamRed = ArgbColor.b(teamColor);
        int teamGreen = ArgbColor.c(teamColor);
        int teamBlue = ArgbColor.d(teamColor);
        if (red == 0) {
            return ArgbColor.a(alpha,
                    (teamRed * green) >> 8,
                    (teamGreen * green) >> 8,
                    (teamBlue * green) >> 8);
        }
        if (green != red) {
            float amount = (green * 0.003921569f) - (red * 0.003921569f);
            return ArgbColor.a(alpha,
                    clamp((int) (red + (teamRed * amount))),
                    clamp((int) (red + (teamGreen * amount))),
                    clamp((int) (red + (teamBlue * amount))));
        }
        return color;
    }

    public static int hueShift(int color, int teamColor) {
        int alpha = ArgbColor.a(color);
        if (alpha == 0) {
            return ArgbColor.b(color) > 0 || ArgbColor.c(color) > 0 || ArgbColor.d(color) > 0 ? 0 : color;
        }
        int red = ArgbColor.b(color);
        int green = ArgbColor.c(color);
        int blue = ArgbColor.d(color);
        float min = Math.min(Math.min(red, green), blue);
        float maxDifference = Math.max(Math.max(Math.abs(red - green), Math.abs(green - blue)), Math.abs(blue - red));
        if (maxDifference <= 15.0f) {
            return color;
        }
        float amount = maxDifference / 255.0f;
        return ArgbColor.a(alpha,
                clamp((int) (min + (ArgbColor.b(teamColor) * amount))),
                clamp((int) (min + (ArgbColor.c(teamColor) * amount))),
                clamp((int) (min + (ArgbColor.d(teamColor) * amount))));
    }

    public static int hueAdd(int color, int teamColor) {
        int alpha = ArgbColor.a(color);
        if (alpha <= 0) {
            return color;
        }
        return ArgbColor.a(alpha,
                clamp((int) (ArgbColor.b(color) + (ArgbColor.b(teamColor) * 0.15f))),
                clamp((int) (ArgbColor.c(color) + (ArgbColor.c(teamColor) * 0.15f))),
                clamp((int) (ArgbColor.d(color) + (ArgbColor.d(teamColor) * 0.15f))));
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
