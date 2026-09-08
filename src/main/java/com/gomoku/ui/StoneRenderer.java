package com.gomoku.ui;

import com.gomoku.model.Stone;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;

final class StoneRenderer {
    private StoneRenderer() {
    }

    static void draw(Graphics2D g2, int cx, int cy, int r, Stone stone, StoneSkin skin,
                     Theme theme, boolean last, int number, double gp) {
        if (r < 2) {
            return;
        }
        Ellipse2D.Double body = new Ellipse2D.Double(cx - r, cy - r, r * 2.0, r * 2.0);
        g2.setColor(new Color(0, 0, 0, skin == StoneSkin.CANDY ? 50 : 70));
        g2.fillOval(cx - r + 2, cy - r + 3, r * 2, r * 2);
        switch (skin) {
            case CANDY -> candy(g2, cx, cy, r, stone, body);
            case GLASS -> glass(g2, cx, cy, r, stone, body);
            case JADE -> jade(g2, cx, cy, r, stone, body);
            case NEON -> neon(g2, cx, cy, r, stone, body);
            case WOOD -> wood(g2, cx, cy, r, stone, body);
            case FLAT -> flat(g2, stone, body);
            default -> classic(g2, cx, cy, r, stone, body);
        }
        if (last) {
            g2.setColor(theme.lastMove);
            int m = Math.max(3, r / 5);
            if (skin == StoneSkin.CANDY) {
                fillStar(g2, cx, cy, m + 2, m / 2 + 1);
            } else if (skin == StoneSkin.GLASS) {
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(cx - m, cy - m, m * 2, m * 2);
            } else {
                g2.fillOval(cx - m, cy - m, m * 2, m * 2);
            }
        }
        if (gp > 22 && number > 0) {
            g2.setFont(Fonts.ui(theme, (float) Math.max(9, gp * 0.28), Font.BOLD));
            g2.setColor(numberColor(stone, skin));
            String t = String.valueOf(number);
            int tw = g2.getFontMetrics().stringWidth(t);
            g2.drawString(t, cx - tw / 2, cy + 4);
        }
    }

    private static void jade(Graphics2D g2, int cx, int cy, int r, Stone stone, Ellipse2D.Double body) {
        if (stone == Stone.BLACK) {
            g2.setPaint(new RadialGradientPaint(cx - r * 0.25f, cy - r * 0.3f, r * 1.2f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0x3A4A38), new Color(0x0C100C)}));
        } else {
            g2.setPaint(new RadialGradientPaint(cx - r * 0.25f, cy - r * 0.3f, r * 1.2f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0xFFF8E8), new Color(0xD4C4A0)}));
        }
        g2.fill(body);
        g2.setColor(stone == Stone.BLACK ? new Color(0x6A8058) : new Color(0xC9A227));
        g2.setStroke(new BasicStroke(1.4f));
        g2.draw(body);
        g2.setColor(new Color(255, 255, 255, stone == Stone.BLACK ? 50 : 140));
        g2.fillOval(cx - r / 2, cy - r / 2, r / 2, r / 3);
    }

    private static void candy(Graphics2D g2, int cx, int cy, int r, Stone stone, Ellipse2D.Double body) {
        Color light = stone == Stone.BLACK ? new Color(0x6B4C9A) : new Color(0xFFF7E0);
        Color dark = stone == Stone.BLACK ? new Color(0x2A1848) : new Color(0xFFD6EC);
        g2.setPaint(new RadialGradientPaint(cx - r * 0.3f, cy - r * 0.35f, r * 1.35f,
                new float[]{0f, 1f}, new Color[]{light, dark}));
        g2.fill(body);
        g2.setColor(stone == Stone.BLACK ? new Color(0xC9A0FF) : new Color(0xFF8FAB));
        g2.setStroke(new BasicStroke(2.6f));
        g2.draw(body);
        g2.setColor(new Color(255, 255, 255, 200));
        g2.fillOval(cx - r / 2, cy - r / 2, Math.max(4, r / 2), Math.max(3, r / 3));
        g2.setColor(new Color(255, 255, 255, 120));
        g2.fillOval(cx + r / 6, cy + r / 5, Math.max(3, r / 5), Math.max(3, r / 5));
    }

    private static void glass(Graphics2D g2, int cx, int cy, int r, Stone stone, Ellipse2D.Double body) {
        Color a = stone == Stone.BLACK ? new Color(0x3A414C) : new Color(0xF5F7FB);
        Color b = stone == Stone.BLACK ? new Color(0x101216) : new Color(0xC5D0DC);
        g2.setPaint(new GradientPaint(cx - r, cy - r, a, cx + r, cy + r, b));
        g2.fill(body);
        g2.setColor(new Color(255, 255, 255, stone == Stone.BLACK ? 45 : 110));
        g2.fillOval(cx - r + 4, cy - r + 3, r * 2 - 10, Math.max(4, r / 2));
        g2.setColor(stone == Stone.BLACK ? new Color(0x8B93A0) : new Color(0x2563EB));
        g2.setStroke(new BasicStroke(stone == Stone.BLACK ? 1.3f : 2.0f));
        g2.draw(body);
    }

    private static void neon(Graphics2D g2, int cx, int cy, int r, Stone stone, Ellipse2D.Double body) {
        g2.setColor(stone == Stone.BLACK ? new Color(40, 40, 50) : new Color(240, 248, 255));
        g2.fill(body);
        g2.setColor(stone == Stone.BLACK ? new Color(120, 80, 255) : new Color(80, 220, 255));
        g2.setStroke(new BasicStroke(2.4f));
        g2.draw(body);
    }

    private static void wood(Graphics2D g2, int cx, int cy, int r, Stone stone, Ellipse2D.Double body) {
        Color a = stone == Stone.BLACK ? new Color(0x4A2C0A) : new Color(0xE8D2A8);
        Color b = stone == Stone.BLACK ? new Color(0x1A0C02) : new Color(0xC4A574);
        g2.setPaint(new GradientPaint(cx - r, cy - r, a, cx + r, cy + r, b));
        g2.fill(body);
    }

    private static void flat(Graphics2D g2, Stone stone, Ellipse2D.Double body) {
        g2.setColor(stone == Stone.BLACK ? Color.BLACK : Color.WHITE);
        g2.fill(body);
        g2.setColor(Color.DARK_GRAY);
        g2.draw(body);
    }

    private static void classic(Graphics2D g2, int cx, int cy, int r, Stone stone, Ellipse2D.Double body) {
        Color a = stone == Stone.BLACK ? new Color(70, 70, 75) : new Color(255, 255, 255);
        Color b = stone == Stone.BLACK ? new Color(10, 10, 12) : new Color(200, 200, 205);
        g2.setPaint(new GradientPaint(cx - r, cy - r, a, cx + r, cy + r, b));
        g2.fill(body);
        g2.setColor(new Color(255, 255, 255, stone == Stone.BLACK ? 70 : 160));
        g2.fillOval(cx - r / 2, cy - r / 2, r / 2, r / 2);
    }

    private static Color numberColor(Stone stone, StoneSkin skin) {
        if (skin == StoneSkin.CANDY) {
            return stone == Stone.BLACK ? Color.WHITE : new Color(0x6B2A4A);
        }
        return stone == Stone.BLACK ? Color.WHITE : Color.BLACK;
    }

    private static void fillStar(Graphics2D g, int cx, int cy, int outer, int inner) {
        java.awt.geom.Path2D p = new java.awt.geom.Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double a = Math.PI / 2 + i * Math.PI / 5;
            double rad = i % 2 == 0 ? outer : inner;
            double x = cx + Math.cos(a) * rad;
            double y = cy - Math.sin(a) * rad;
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
