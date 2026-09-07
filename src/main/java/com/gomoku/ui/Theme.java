package com.gomoku.ui;

import java.awt.Color;

public final class Theme {
    public final String id;
    public final String label;
    public final Color board;
    public final Color line;
    public final Color star;
    public final Color labelFg;
    public final Color panelBg;
    public final Color panelFg;
    public final Color accent;
    public final Color lastMove;
    public final Color forbidden;

    public Theme(String id, String label, Color board, Color line, Color star, Color labelFg,
                 Color panelBg, Color panelFg, Color accent, Color lastMove, Color forbidden) {
        this.id = id;
        this.label = label;
        this.board = board;
        this.line = line;
        this.star = star;
        this.labelFg = labelFg;
        this.panelBg = panelBg;
        this.panelFg = panelFg;
        this.accent = accent;
        this.lastMove = lastMove;
        this.forbidden = forbidden;
    }

    public static Theme wood() {
        return new Theme("wood", "木质",
                new Color(0xD7B07A), new Color(0x5A3A1C), new Color(0x3A2208),
                new Color(0x3A2208), new Color(0x2B2118), new Color(0xF4E6D0),
                new Color(0xC45C26), new Color(0xE03A3A), new Color(0xCC3333));
    }

    public static Theme dark() {
        return new Theme("dark", "暗色",
                new Color(0x2A2E35), new Color(0x9AA3AD), new Color(0xE8EEF4),
                new Color(0xE8EEF4), new Color(0x16181C), new Color(0xE8EEF4),
                new Color(0x5B8DEF), new Color(0xFFC14D), new Color(0xFF6B6B));
    }

    public static Theme contrast() {
        return new Theme("contrast", "高对比",
                Color.WHITE, Color.BLACK, Color.BLACK, Color.BLACK,
                Color.BLACK, Color.WHITE, new Color(0x0066FF), Color.RED, Color.RED);
    }

    public static Theme byId(String id) {
        return switch (id) {
            case "dark" -> dark();
            case "contrast" -> contrast();
            default -> wood();
        };
    }

    public static Theme[] all() {
        return new Theme[]{wood(), dark(), contrast()};
    }
}
