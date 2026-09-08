package com.gomoku.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Path2D;
import java.util.Random;

final class DecorPainter {
    private DecorPainter() {
    }

    static void paintFelt(Graphics2D g, int w, int h, Theme theme) {
        switch (theme.family) {
            case GUFENG -> inkWash(g, w, h, theme);
            case CARTOON -> candySky(g, w, h, theme);
            case MODERN -> modernDesk(g, w, h, theme);
            case CLASSIC -> {
                g.setPaint(new GradientPaint(0, 0, theme.felt, w, h, theme.panelBg));
                g.fillRect(0, 0, w, h);
            }
        }
    }

    static void paintBoard(Graphics2D g, int ox, int oy, double gp, int n, Theme theme) {
        int pad = switch (theme.family) {
            case GUFENG -> 28;
            case CARTOON -> 26;
            case MODERN -> 18;
            case CLASSIC -> 16;
        };
        int x = ox - pad;
        int y = oy - pad;
        int side = (int) Math.round(gp * (n - 1)) + pad * 2;
        switch (theme.family) {
            case GUFENG -> gufengBoard(g, x, y, side, theme);
            case CARTOON -> cartoonBoard(g, x, y, side, theme);
            case MODERN -> modernBoard(g, x, y, side, theme);
            case CLASSIC -> classicBoard(g, x, y, side, theme);
        }
    }

    static void paintStars(Graphics2D g, int ox, int oy, double gp, int[] stars, Theme theme) {
        for (int sx : stars) {
            for (int sy : stars) {
                int cx = (int) Math.round(ox + sx * gp);
                int cy = (int) Math.round(oy + sy * gp);
                switch (theme.family) {
                    case GUFENG -> {
                        g.setColor(theme.star);
                        g.fillOval(cx - 4, cy - 4, 8, 8);
                        g.setStroke(new BasicStroke(1.1f));
                        g.drawOval(cx - 7, cy - 7, 14, 14);
                    }
                    case CARTOON -> {
                        g.setColor(theme.star);
                        fillStar(g, cx, cy, 7, 3);
                    }
                    case MODERN -> {
                        g.setColor(theme.star);
                        g.fillRect(cx - 3, cy - 3, 6, 6);
                    }
                    case CLASSIC -> {
                        g.setColor(theme.star);
                        g.fillOval(cx - 4, cy - 4, 8, 8);
                    }
                }
            }
        }
    }

    static void paintOrnaments(Graphics2D g, int w, int h, Theme theme) {
        if (theme.family == Theme.Family.GUFENG) {
            g.setFont(Fonts.title(theme, 13f));
            g.setColor(new Color(theme.frameTrim.getRed(), theme.frameTrim.getGreen(),
                    theme.frameTrim.getBlue(), 180));
            g.drawString("弈", 16, 28);
            g.setFont(Fonts.ui(theme, 11f, Font.PLAIN));
            g.setColor(theme.panelMuted);
            String poem = theme.tagline;
            g.drawString(poem, w - g.getFontMetrics().stringWidth(poem) - 16, h - 14);
        } else if (theme.family == Theme.Family.CARTOON) {
            g.setColor(new Color(255, 255, 255, 160));
            g.fillOval(w - 70, 12, 28, 28);
            g.fillOval(w - 52, 20, 22, 22);
            g.fillOval(18, h - 48, 26, 26);
            g.fillOval(36, h - 40, 20, 20);
        }
    }

    private static void inkWash(Graphics2D g, int w, int h, Theme theme) {
        g.setPaint(new GradientPaint(0, 0, new Color(0x1A120C), w, h, new Color(0x3A2416)));
        g.fillRect(0, 0, w, h);
        Random rng = new Random(42);
        for (int i = 0; i < 90; i++) {
            int x = rng.nextInt(Math.max(1, w));
            int y = rng.nextInt(Math.max(1, h));
            int s = 8 + rng.nextInt(40);
            g.setColor(new Color(255, 230, 180, 8 + rng.nextInt(12)));
            g.fillOval(x, y, s, s);
        }
        Path2D far = mountain(w, h, 0.62, new int[]{80, 140, 90, 160, 70});
        g.setColor(new Color(20, 16, 12, 70));
        g.fill(far);
        Path2D near = mountain(w, h, 0.74, new int[]{120, 80, 150, 60, 110});
        g.setColor(new Color(12, 10, 8, 110));
        g.fill(near);
        g.setColor(new Color(255, 255, 255, 28));
        g.fillOval(w / 8, h / 10, 90, 28);
        g.fillOval(w / 8 + 40, h / 10 + 8, 70, 22);
        g.fillOval(w * 2 / 3, h / 7, 110, 30);
        g.fillOval(w * 2 / 3 + 50, h / 7 + 10, 80, 22);
        g.setColor(new Color(theme.frameTrim.getRed(), theme.frameTrim.getGreen(),
                theme.frameTrim.getBlue(), 40));
        g.setStroke(new BasicStroke(1.2f));
        g.drawLine(24, 36, w - 24, 36);
    }

    private static Path2D mountain(int w, int h, double baseY, int[] peaks) {
        Path2D p = new Path2D.Double();
        double y0 = h * baseY;
        p.moveTo(0, h);
        p.lineTo(0, y0);
        int step = Math.max(1, w / peaks.length);
        for (int i = 0; i < peaks.length; i++) {
            double x = step * i + step / 2.0;
            p.curveTo(x - 30, y0, x - 10, y0 - peaks[i], x, y0 - peaks[i]);
            p.curveTo(x + 10, y0 - peaks[i], x + 30, y0, x + step / 2.0, y0);
        }
        p.lineTo(w, y0);
        p.lineTo(w, h);
        p.closePath();
        return p;
    }

    private static void candySky(Graphics2D g, int w, int h, Theme theme) {
        g.setPaint(new GradientPaint(0, 0, new Color(0xB8E0FF), 0, h, theme.felt));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(255, 255, 255, 90));
        g.fillOval(-40, -20, 180, 90);
        g.fillOval(w - 160, 10, 200, 100);
        g.fillOval(w / 3, h - 80, 220, 90);
        Random rng = new Random(9);
        Color[] dots = {
                new Color(255, 107, 157, 90),
                new Color(126, 200, 227, 90),
                new Color(255, 214, 90, 90),
                new Color(180, 140, 255, 80)
        };
        for (int i = 0; i < 18; i++) {
            g.setColor(dots[i % dots.length]);
            int s = 8 + rng.nextInt(16);
            g.fillOval(rng.nextInt(Math.max(1, w)), rng.nextInt(Math.max(1, h)), s, s);
        }
    }

    private static void modernDesk(Graphics2D g, int w, int h, Theme theme) {
        g.setPaint(new GradientPaint(0, 0, new Color(0xEEF1F5), w, h, new Color(0xD9DEE6)));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(255, 255, 255, 70));
        g.setStroke(new BasicStroke(1f));
        for (int i = 40; i < w; i += 40) {
            g.drawLine(i, 0, i, h);
        }
        for (int i = 40; i < h; i += 40) {
            g.drawLine(0, i, w, i);
        }
        g.setPaint(new RadialGradientPaint(w * 0.5f, h * 0.35f, Math.max(80f, Math.max(w, h) * 0.55f),
                new float[]{0f, 1f},
                new Color[]{new Color(255, 255, 255, 90), new Color(255, 255, 255, 0)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE));
        g.fillRect(0, 0, w, h);
    }

    private static void gufengBoard(Graphics2D g, int x, int y, int side, Theme theme) {
        g.setColor(new Color(0, 0, 0, 70));
        g.fillRoundRect(x + 8, y + 10, side, side, 8, 8);
        g.setPaint(new GradientPaint(x, y, theme.frame, x + side, y + side, theme.frame.darker()));
        g.fillRoundRect(x, y, side, side, 10, 10);
        g.setColor(theme.frameTrim);
        g.setStroke(new BasicStroke(2.4f));
        g.drawRoundRect(x + 6, y + 6, side - 12, side - 12, 6, 6);
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(x + 11, y + 11, side - 22, side - 22, 4, 4);
        int inner = 18;
        silk(g, x + inner, y + inner, side - inner * 2, theme);
        cornerBrace(g, x + 14, y + 14, 18, theme.frameTrim, false, false);
        cornerBrace(g, x + side - 14, y + 14, 18, theme.frameTrim, true, false);
        cornerBrace(g, x + 14, y + side - 14, 18, theme.frameTrim, false, true);
        cornerBrace(g, x + side - 14, y + side - 14, 18, theme.frameTrim, true, true);
        g.setColor(new Color(theme.accent.getRed(), theme.accent.getGreen(), theme.accent.getBlue(), 200));
        g.fillRoundRect(x + side - 46, y + side - 46, 26, 26, 3, 3);
        g.setColor(new Color(0xF8E7C0));
        g.setFont(Fonts.title(theme, 13f));
        g.drawString("五", x + side - 40, y + side - 28);
    }

    private static void silk(Graphics2D g, int x, int y, int side, Theme theme) {
        g.setPaint(new GradientPaint(x, y, theme.board, x + side, y + side, theme.boardAlt));
        g.fillRect(x, y, side, side);
        Random rng = new Random(21);
        for (int i = 0; i < 50; i++) {
            int yy = y + rng.nextInt(Math.max(1, side));
            g.setColor(new Color(90, 60, 20, 18));
            g.drawLine(x, yy, x + side, yy + rng.nextInt(7) - 3);
        }
        g.setPaint(new GradientPaint(x, y, new Color(255, 255, 255, 50), x + side, y,
                new Color(255, 255, 255, 0)));
        g.fillRect(x, y, side, side / 3);
    }

    private static void cartoonBoard(Graphics2D g, int x, int y, int side, Theme theme) {
        g.setColor(new Color(80, 40, 70, 50));
        g.fillRoundRect(x + 10, y + 14, side, side, 36, 36);
        g.setPaint(new GradientPaint(x, y, theme.frame, x + side, y + side, theme.frameTrim));
        g.fillRoundRect(x, y, side, side, 34, 34);
        g.setColor(theme.frameTrim);
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(x + 8, y + 8, side - 16, side - 16, 26, 26);
        g.setPaint(new GradientPaint(x, y, theme.board, x, y + side, theme.boardAlt));
        g.fillRoundRect(x + 18, y + 18, side - 36, side - 36, 18, 18);
        g.setColor(new Color(255, 255, 255, 90));
        g.fillRoundRect(x + 26, y + 24, side - 70, 22, 12, 12);
    }

    private static void modernBoard(Graphics2D g, int x, int y, int side, Theme theme) {
        g.setColor(new Color(15, 23, 42, 28));
        g.fillRoundRect(x + 10, y + 14, side, side, 18, 18);
        g.setColor(theme.cardBg);
        g.fillRoundRect(x, y, side, side, 16, 16);
        g.setColor(theme.frame);
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(x, y, side, side, 16, 16);
        g.setPaint(new GradientPaint(x, y, theme.board, x + side, y + side, theme.boardAlt));
        g.fillRoundRect(x + 12, y + 12, side - 24, side - 24, 8, 8);
        g.setColor(new Color(theme.accent.getRed(), theme.accent.getGreen(), theme.accent.getBlue(), 40));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(x + 12, y + 12, side - 24, side - 24, 8, 8);
    }

    private static void classicBoard(Graphics2D g, int x, int y, int side, Theme theme) {
        g.setPaint(new GradientPaint(x, y, theme.board, x + side, y + side, theme.boardAlt));
        g.fillRect(x, y, side, side);
        g.setColor(theme.frame);
        g.setStroke(new BasicStroke(3f));
        g.drawRect(x + 4, y + 4, side - 8, side - 8);
    }

    private static void cornerBrace(Graphics2D g, int x, int y, int len, Color c, boolean flipX, boolean flipY) {
        g.setColor(c);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
        int dx = flipX ? -1 : 1;
        int dy = flipY ? -1 : 1;
        g.drawLine(x, y, x + dx * len, y);
        g.drawLine(x, y, x, y + dy * len);
    }

    private static void fillStar(Graphics2D g, int cx, int cy, int outer, int inner) {
        Path2D p = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double a = Math.PI / 2 + i * Math.PI / 5;
            double r = i % 2 == 0 ? outer : inner;
            double x = cx + Math.cos(a) * r;
            double y = cy - Math.sin(a) * r;
            if (i == 0) {
                p.moveTo(x, y);
            } else {
                p.lineTo(x, y);
            }
        }
        p.closePath();
        g.fill(p);
    }

}
