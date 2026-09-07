package com.gomoku.ui;

import com.gomoku.model.Board;
import com.gomoku.model.ForbiddenAnalyzer;
import com.gomoku.model.ForbiddenKind;
import com.gomoku.model.Game;
import com.gomoku.model.Move;
import com.gomoku.model.Pos;
import com.gomoku.model.Stone;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.function.BiConsumer;

public final class BoardPanel extends JPanel {
    private Game game;
    private Theme theme;
    private StoneSkin skin;
    private BiConsumer<Integer, Integer> onClick;
    private double anim = 1;
    private Point hover;
    private Pos effectPos;
    private float effectT;
    private final Timer animTimer;

    public BoardPanel() {
        setPreferredSize(new Dimension(640, 640));
        animTimer = new Timer(16, e -> {
            boolean dirty = false;
            if (anim < 1) {
                anim = Math.min(1, anim + 0.12);
                dirty = true;
            }
            if (effectPos != null) {
                effectT += 0.05f;
                if (effectT >= 1) {
                    effectPos = null;
                }
                dirty = true;
            }
            if (dirty) {
                repaint();
            }
        });
        animTimer.start();
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hover = e.getPoint();
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = null;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (game == null || onClick == null) {
                    return;
                }
                Pos p = toBoard(e.getX(), e.getY());
                if (p != null) {
                    onClick.accept(p.x, p.y);
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void setGame(Game game) {
        this.game = game;
        this.theme = Theme.byId(game.settings().themeId);
        this.skin = StoneSkin.byId(game.settings().skinId);
        bounce();
        repaint();
    }

    public void setOnClick(BiConsumer<Integer, Integer> onClick) {
        this.onClick = onClick;
    }

    public void bounce() {
        if (game != null && game.settings().animation) {
            anim = 0.35;
        } else {
            anim = 1;
        }
        if (game != null && !game.moves().isEmpty() && game.settings().effects) {
            Move last = game.moves().get(game.moves().size() - 1);
            effectPos = last.pos();
            effectT = 0;
        }
        repaint();
    }

    public void applyLook(Theme theme, StoneSkin skin) {
        this.theme = theme;
        this.skin = skin;
        if (game != null) {
            game.settings().themeId = theme.id;
            game.settings().skinId = skin.id;
        }
        setBackground(theme.panelBg);
        repaint();
    }

    private int margin() {
        return 36;
    }

    private double gap() {
        int n = game == null ? 15 : game.board().size();
        int inner = Math.min(getWidth(), getHeight()) - margin() * 2;
        return inner / (double) (n - 1);
    }

    private int originX() {
        int n = game == null ? 15 : game.board().size();
        double g = gap();
        return (int) ((getWidth() - g * (n - 1)) / 2);
    }

    private int originY() {
        int n = game == null ? 15 : game.board().size();
        double g = gap();
        return (int) ((getHeight() - g * (n - 1)) / 2);
    }

    public Pos toBoard(int px, int py) {
        if (game == null) {
            return null;
        }
        double g = gap();
        int x = (int) Math.round((px - originX()) / g);
        int y = (int) Math.round((py - originY()) / g);
        if (game.board().inBounds(x, y)) {
            return new Pos(x, y);
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (theme == null) {
            theme = Theme.wood();
        }
        int w = getWidth();
        int h = getHeight();
        g2.setPaint(new GradientPaint(0, 0, theme.board, w, h, theme.board.darker()));
        g2.fillRect(0, 0, w, h);
        if (game == null) {
            g2.dispose();
            return;
        }
        Board board = game.board();
        int n = board.size();
        double gp = gap();
        int ox = originX();
        int oy = originY();
        g2.setColor(theme.line);
        g2.setStroke(new BasicStroke(1.2f));
        for (int i = 0; i < n; i++) {
            int x = (int) Math.round(ox + i * gp);
            int y = (int) Math.round(oy + i * gp);
            g2.drawLine(ox, y, (int) Math.round(ox + (n - 1) * gp), y);
            g2.drawLine(x, oy, x, (int) Math.round(oy + (n - 1) * gp));
        }
        g2.setColor(theme.star);
        int[] stars = starIndexes(n);
        for (int sx : stars) {
            for (int sy : stars) {
                int cx = (int) Math.round(ox + sx * gp);
                int cy = (int) Math.round(oy + sy * gp);
                g2.fillOval(cx - 4, cy - 4, 8, 8);
            }
        }
        g2.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        g2.setColor(theme.labelFg);
        for (int i = 0; i < n; i++) {
            String col = String.valueOf((char) ('A' + i));
            String row = String.valueOf(i + 1);
            int x = (int) Math.round(ox + i * gp);
            int y = (int) Math.round(oy + i * gp);
            g2.drawString(col, x - 4, oy - 12);
            g2.drawString(row, ox - 22, y + 4);
        }
        if (game.settings().showForbiddenHints && game.toMove() == Stone.BLACK && game.settings().renjuForbidden) {
            g2.setColor(new Color(theme.forbidden.getRed(), theme.forbidden.getGreen(), theme.forbidden.getBlue(), 90));
            for (int y = 0; y < n; y++) {
                for (int x = 0; x < n; x++) {
                    if (!board.isEmpty(x, y)) {
                        continue;
                    }
                    ForbiddenKind k = ForbiddenAnalyzer.previewForbidden(board, x, y, true);
                    if (k != ForbiddenKind.NONE) {
                        int cx = (int) Math.round(ox + x * gp);
                        int cy = (int) Math.round(oy + y * gp);
                        g2.drawLine(cx - 6, cy - 6, cx + 6, cy + 6);
                        g2.drawLine(cx - 6, cy + 6, cx + 6, cy - 6);
                    }
                }
            }
        }
        int last = game.moves().size() - 1;
        int blind = game.settings().blindLastMoves;
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                Stone stone = board.get(x, y);
                if (stone == null) {
                    continue;
                }
                int moveIndex = -1;
                for (int i = 0; i < game.moves().size(); i++) {
                    Move m = game.moves().get(i);
                    if (m.x == x && m.y == y) {
                        moveIndex = i;
                    }
                }
                if (blind > 0 && (moveIndex < 0 || moveIndex < game.moves().size() - blind)) {
                    continue;
                }
                boolean newest = moveIndex == last && last >= 0;
                double scale = newest ? anim : 1;
                drawStone(g2, ox, oy, gp, x, y, stone, scale, moveIndex + 1, newest);
            }
        }
        if (game.pending() != null) {
            Pos p = game.pending();
            g2.setColor(new Color(0, 120, 255, 120));
            int cx = (int) Math.round(ox + p.x * gp);
            int cy = (int) Math.round(oy + p.y * gp);
            int r = (int) (gp * 0.38);
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        }
        if (hover != null) {
            Pos hp = toBoard(hover.x, hover.y);
            if (hp != null && board.isEmpty(hp.x, hp.y)) {
                g2.setColor(new Color(0, 0, 0, 40));
                int cx = (int) Math.round(ox + hp.x * gp);
                int cy = (int) Math.round(oy + hp.y * gp);
                int r = (int) (gp * 0.2);
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);
            }
        }
        if (effectPos != null) {
            int cx = (int) Math.round(ox + effectPos.x * gp);
            int cy = (int) Math.round(oy + effectPos.y * gp);
            int r = (int) (gp * (0.4 + effectT * 0.8));
            g2.setColor(new Color(theme.accent.getRed(), theme.accent.getGreen(), theme.accent.getBlue(),
                    (int) (140 * (1 - effectT))));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        }
        g2.dispose();
    }

    private void drawStone(Graphics2D g2, int ox, int oy, double gp, int x, int y, Stone stone,
                           double scale, int number, boolean last) {
        int cx = (int) Math.round(ox + x * gp);
        int cy = (int) Math.round(oy + y * gp);
        int r = (int) (gp * 0.42 * scale);
        Ellipse2D.Double e = new Ellipse2D.Double(cx - r, cy - r, r * 2.0, r * 2.0);
        if (skin == StoneSkin.NEON) {
            g2.setColor(stone == Stone.BLACK ? new Color(40, 40, 50) : new Color(240, 248, 255));
            g2.fill(e);
            g2.setColor(stone == Stone.BLACK ? new Color(120, 80, 255) : new Color(80, 220, 255));
            g2.setStroke(new BasicStroke(2.4f));
            g2.draw(e);
        } else if (skin == StoneSkin.FLAT) {
            g2.setColor(stone == Stone.BLACK ? Color.BLACK : Color.WHITE);
            g2.fill(e);
            g2.setColor(Color.DARK_GRAY);
            g2.draw(e);
        } else if (skin == StoneSkin.WOOD) {
            Color a = stone == Stone.BLACK ? new Color(0x4A2C0A) : new Color(0xE8D2A8);
            Color b = stone == Stone.BLACK ? new Color(0x1A0C02) : new Color(0xC4A574);
            g2.setPaint(new GradientPaint(cx - r, cy - r, a, cx + r, cy + r, b));
            g2.fill(e);
        } else {
            Color a = stone == Stone.BLACK ? new Color(70, 70, 75) : new Color(255, 255, 255);
            Color b = stone == Stone.BLACK ? new Color(10, 10, 12) : new Color(200, 200, 205);
            g2.setPaint(new GradientPaint(cx - r, cy - r, a, cx + r, cy + r, b));
            g2.fill(e);
            g2.setColor(new Color(255, 255, 255, stone == Stone.BLACK ? 70 : 160));
            g2.fillOval(cx - r / 2, cy - r / 2, r / 2, r / 2);
        }
        if (last) {
            g2.setColor(theme.lastMove);
            int m = Math.max(3, r / 5);
            g2.fillOval(cx - m, cy - m, m * 2, m * 2);
        }
        if (gp > 22 && number > 0) {
            g2.setFont(getFont().deriveFont(Font.BOLD, (float) Math.max(9, gp * 0.28)));
            g2.setColor(stone == Stone.BLACK ? Color.WHITE : Color.BLACK);
            String t = String.valueOf(number);
            int tw = g2.getFontMetrics().stringWidth(t);
            g2.drawString(t, cx - tw / 2, cy + 4);
        }
    }

    private int[] starIndexes(int n) {
        if (n < 13) {
            return new int[]{n / 2};
        }
        int s = n == 15 ? 3 : (n == 19 ? 3 : 2);
        return new int[]{s, n / 2, n - 1 - s};
    }
}
