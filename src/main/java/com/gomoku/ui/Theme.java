package com.gomoku.ui;

import java.awt.Color;

public final class Theme {
    public enum Family {
        GUFENG, CARTOON, MODERN, CLASSIC
    }

    public final String id;
    public final String label;
    public final String tagline;
    public final Family family;
    public final String defaultSkinId;
    public final Color board;
    public final Color boardAlt;
    public final Color line;
    public final Color star;
    public final Color labelFg;
    public final Color panelBg;
    public final Color panelFg;
    public final Color panelMuted;
    public final Color accent;
    public final Color accentSoft;
    public final Color lastMove;
    public final Color forbidden;
    public final Color frame;
    public final Color frameTrim;
    public final Color cardBg;
    public final Color buttonBg;
    public final Color buttonFg;
    public final Color titleFg;
    public final Color felt;

    private Theme(Builder b) {
        this.id = b.id;
        this.label = b.label;
        this.tagline = b.tagline;
        this.family = b.family;
        this.defaultSkinId = b.defaultSkinId;
        this.board = b.board;
        this.boardAlt = b.boardAlt;
        this.line = b.line;
        this.star = b.star;
        this.labelFg = b.labelFg;
        this.panelBg = b.panelBg;
        this.panelFg = b.panelFg;
        this.panelMuted = b.panelMuted;
        this.accent = b.accent;
        this.accentSoft = b.accentSoft;
        this.lastMove = b.lastMove;
        this.forbidden = b.forbidden;
        this.frame = b.frame;
        this.frameTrim = b.frameTrim;
        this.cardBg = b.cardBg;
        this.buttonBg = b.buttonBg;
        this.buttonFg = b.buttonFg;
        this.titleFg = b.titleFg;
        this.felt = b.felt;
    }

    public static Theme gufeng() {
        return new Builder()
                .id("gufeng").label("古风").tagline("水墨丹青 · 落子无悔")
                .family(Family.GUFENG).skin("jade")
                .board(0xE8D29A).boardAlt(0xCFAE6E)
                .line(0x3A2612).star(0x2A1808).label(0x4A3014)
                .panel(0x1C1410).fg(0xF3E6C8).muted(0xB8A07A)
                .accent(0xC23A2B).accentSoft(0x8B2E22)
                .last(0xC23A2B).forbid(0x8B1E1E)
                .frame(0x4A2A14).trim(0xC9A227)
                .card(0x2E2218).button(0x6B3A22, 0xF8E7C0)
                .title(0xE8C872).felt(0x241810)
                .build();
    }

    public static Theme cartoon() {
        return new Builder()
                .id("cartoon").label("卡通").tagline("糖果乐园 · 连五就赢")
                .family(Family.CARTOON).skin("candy")
                .board(0xFFF4C8).boardAlt(0xFFE08A)
                .line(0xE07A9A).star(0xFF6B9D).label(0x8B3A6A)
                .panel(0xFFF0F6).fg(0x4A2A5A).muted(0xB07090)
                .accent(0xFF6B9D).accentSoft(0xFFB3C9)
                .last(0xFF4D8D).forbid(0xFF4D6A)
                .frame(0x7EC8E3).trim(0xFF8FAB)
                .card(0xFFFFFF).button(0xFF8FAB, 0xFFFFFF)
                .title(0xC43A7A).felt(0xFFD6E8)
                .build();
    }

    public static Theme modern() {
        return new Builder()
                .id("modern").label("现代").tagline("极简玻璃 · 专注五连")
                .family(Family.MODERN).skin("glass")
                .board(0xF7F8FA).boardAlt(0xEBEEF3)
                .line(0xC5CAD3).star(0x2563EB).label(0x6B7280)
                .panel(0xF2F4F7).fg(0x1C1F24).muted(0x6B7280)
                .accent(0x2563EB).accentSoft(0x93C5FD)
                .last(0x2563EB).forbid(0xEF4444)
                .frame(0xE5E7EB).trim(0x2563EB)
                .card(0xFFFFFF).button(0x2563EB, 0xFFFFFF)
                .title(0x111827).felt(0xE8ECF1)
                .build();
    }

    public static Theme wood() {
        return new Builder()
                .id("wood").label("棋院").tagline("木质棋盘 · 经典对弈")
                .family(Family.CLASSIC).skin("wood")
                .board(0xD7B07A).boardAlt(0xC49A5C)
                .line(0x5A3A1C).star(0x3A2208).label(0x3A2208)
                .panel(0x2B2118).fg(0xF4E6D0).muted(0xC4B090)
                .accent(0xC45C26).accentSoft(0xA04820)
                .last(0xE03A3A).forbid(0xCC3333)
                .frame(0x5A3A1C).trim(0xC45C26)
                .card(0x3A2C20).button(0xC45C26, 0xFFF4E4)
                .title(0xF4E6D0).felt(0x2B2118)
                .build();
    }

    public static Theme dark() {
        return new Builder()
                .id("dark").label("暗夜").tagline("霓虹夜弈 · 光影落子")
                .family(Family.CLASSIC).skin("neon")
                .board(0x2A2E35).boardAlt(0x1C2026)
                .line(0x9AA3AD).star(0xE8EEF4).label(0xE8EEF4)
                .panel(0x16181C).fg(0xE8EEF4).muted(0x9AA3AD)
                .accent(0x5B8DEF).accentSoft(0x3A5A9A)
                .last(0xFFC14D).forbid(0xFF6B6B)
                .frame(0x0E1014).trim(0x5B8DEF)
                .card(0x1E2228).button(0x5B8DEF, 0xFFFFFF)
                .title(0xE8EEF4).felt(0x121418)
                .build();
    }

    public static Theme contrast() {
        return new Builder()
                .id("contrast").label("高对比").tagline("黑白分明 · 清晰易读")
                .family(Family.CLASSIC).skin("flat")
                .board(0xFFFFFF).boardAlt(0xF0F0F0)
                .line(0x000000).star(0x000000).label(0x000000)
                .panel(0x000000).fg(0xFFFFFF).muted(0xCCCCCC)
                .accent(0x0066FF).accentSoft(0x3399FF)
                .last(0xFF0000).forbid(0xFF0000)
                .frame(0x000000).trim(0x0066FF)
                .card(0x111111).button(0x0066FF, 0xFFFFFF)
                .title(0xFFFFFF).felt(0x000000)
                .build();
    }

    public static Theme byId(String id) {
        return switch (id == null ? "" : id) {
            case "cartoon" -> cartoon();
            case "modern" -> modern();
            case "wood" -> wood();
            case "dark" -> dark();
            case "contrast" -> contrast();
            default -> gufeng();
        };
    }

    public static Theme[] featured() {
        return new Theme[]{gufeng(), cartoon(), modern()};
    }

    public static Theme[] extras() {
        return new Theme[]{wood(), dark(), contrast()};
    }

    public static Theme[] all() {
        return new Theme[]{gufeng(), cartoon(), modern(), wood(), dark(), contrast()};
    }

    public static boolean isKnown(String id) {
        for (Theme t : all()) {
            if (t.id.equals(id)) {
                return true;
            }
        }
        return false;
    }

    private static final class Builder {
        private String id;
        private String label;
        private String tagline;
        private Family family;
        private String defaultSkinId;
        private Color board;
        private Color boardAlt;
        private Color line;
        private Color star;
        private Color labelFg;
        private Color panelBg;
        private Color panelFg;
        private Color panelMuted;
        private Color accent;
        private Color accentSoft;
        private Color lastMove;
        private Color forbidden;
        private Color frame;
        private Color frameTrim;
        private Color cardBg;
        private Color buttonBg;
        private Color buttonFg;
        private Color titleFg;
        private Color felt;

        Builder id(String v) {
            id = v;
            return this;
        }

        Builder label(String v) {
            label = v;
            return this;
        }

        Builder tagline(String v) {
            tagline = v;
            return this;
        }

        Builder family(Family v) {
            family = v;
            return this;
        }

        Builder skin(String v) {
            defaultSkinId = v;
            return this;
        }

        Builder board(int rgb) {
            board = new Color(rgb);
            return this;
        }

        Builder boardAlt(int rgb) {
            boardAlt = new Color(rgb);
            return this;
        }

        Builder line(int rgb) {
            line = new Color(rgb);
            return this;
        }

        Builder star(int rgb) {
            star = new Color(rgb);
            return this;
        }

        Builder label(int rgb) {
            labelFg = new Color(rgb);
            return this;
        }

        Builder panel(int rgb) {
            panelBg = new Color(rgb);
            return this;
        }

        Builder fg(int rgb) {
            panelFg = new Color(rgb);
            return this;
        }

        Builder muted(int rgb) {
            panelMuted = new Color(rgb);
            return this;
        }

        Builder accent(int rgb) {
            accent = new Color(rgb);
            return this;
        }

        Builder accentSoft(int rgb) {
            accentSoft = new Color(rgb);
            return this;
        }

        Builder last(int rgb) {
            lastMove = new Color(rgb);
            return this;
        }

        Builder forbid(int rgb) {
            forbidden = new Color(rgb);
            return this;
        }

        Builder frame(int rgb) {
            frame = new Color(rgb);
            return this;
        }

        Builder trim(int rgb) {
            frameTrim = new Color(rgb);
            return this;
        }

        Builder card(int rgb) {
            cardBg = new Color(rgb);
            return this;
        }

        Builder button(int bg, int fg) {
            buttonBg = new Color(bg);
            buttonFg = new Color(fg);
            return this;
        }

        Builder title(int rgb) {
            titleFg = new Color(rgb);
            return this;
        }

        Builder felt(int rgb) {
            felt = new Color(rgb);
            return this;
        }

        Theme build() {
            return new Theme(this);
        }
    }
}
