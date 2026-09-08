package com.gomoku.ui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class Fonts {
    private static final Set<String> AVAILABLE = Arrays.stream(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
            .collect(Collectors.toSet());

    private Fonts() {
    }

    public static Font ui(Theme theme, float size, int style) {
        return switch (theme.family) {
            case GUFENG -> pick(size, style, "Noto Serif CJK SC", "Source Han Serif SC",
                    "Songti SC", "STSong", "SimSun", "Serif");
            case CARTOON -> pick(size, style, "Noto Sans CJK SC", "Source Han Sans SC",
                    "Comic Sans MS", "Rounded Mplus 1c", "Dialog");
            case MODERN -> pick(size, style, "Noto Sans CJK SC", "Source Han Sans SC",
                    "PingFang SC", "Microsoft YaHei", "SansSerif");
            case CLASSIC -> pick(size, style, "Noto Sans CJK SC", "Dialog", "SansSerif");
        };
    }

    public static Font title(Theme theme, float size) {
        return ui(theme, size, Font.BOLD);
    }

    private static Font pick(float size, int style, String... names) {
        for (String name : names) {
            if (AVAILABLE.contains(name)) {
                return new Font(name, style, Math.round(size));
            }
        }
        return new Font(Font.SANS_SERIF, style, Math.round(size));
    }
}
