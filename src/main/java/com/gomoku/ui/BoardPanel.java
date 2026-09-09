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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
        setBackground(theme.felt);
        repaint();
    }

    private int margin() {
        if (theme == null) {
            return 40;
        }
        return switch (theme.family) {
            case GUFENG -> 56;
            case CARTOON -> 52;
            case MODERN -> 44;
            case CLASSIC -> 40;
        };
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
            theme = Theme.gufeng();
        }
        int w = getWidth();
        int h = getHeight();
        DecorPainter.paintFelt(g2, w, h, theme);
        if (game == null) {
            DecorPainter.paintOrnaments(g2, w, h, theme);
            g2.dispose();
            return;
        }
        Board board = game.board();
        int n = board.size();
        double gp = gap();
        int ox = originX();
        int oy = originY();
        DecorPainter.paintBoard(g2, ox, oy, gp, n, theme);
        g2.setColor(theme.line);
        float lineW = theme.family == Theme.Family.MODERN ? 1.0f : theme.family == Theme.Family.CARTOON ? 1.8f : 1.3f;
        g2.setStroke(new BasicStroke(lineW));
        for (int i = 0; i < n; i++) {
            int x = (int) Math.round(ox + i * gp);
            int y = (int) Math.round(oy + i * gp);
            g2.drawLine(ox, y, (int) Math.round(ox + (n - 1) * gp), y);
            g2.drawLine(x, oy, x, (int) Math.round(oy + (n - 1) * gp));
        }
        DecorPainter.paintStars(g2, ox, oy, gp, starIndexes(n), theme);
        g2.setFont(Fonts.ui(theme, 11f, Font.PLAIN));
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
        if (!game.winningLine().isEmpty()) {
            g2.setColor(new Color(theme.lastMove.getRed(), theme.lastMove.getGreen(),
                    theme.lastMove.getBlue(), 210));
            g2.setStroke(new BasicStroke(3.5f));
            for (Pos p : game.winningLine()) {
                int cx = (int) Math.round(ox + p.x * gp);
                int cy = (int) Math.round(oy + p.y * gp);
                int r = (int) (gp * 0.46);
                g2.drawOval(cx - r, cy - r, r * 2, r * 2);
            }
        }
        DecorPainter.paintOrnaments(g2, w, h, theme);
        g2.dispose();
    }

    private void drawStone(Graphics2D g2, int ox, int oy, double gp, int x, int y, Stone stone,
                           double scale, int number, boolean last) {
        int cx = (int) Math.round(ox + x * gp);
        int cy = (int) Math.round(oy + y * gp);
        int r = (int) (gp * 0.42 * scale);
        StoneRenderer.draw(g2, cx, cy, r, stone, skin == null ? StoneSkin.JADE : skin,
                theme, last, number, gp);
    }

    private int[] starIndexes(int n) {
        if (n < 13) {
            return new int[]{n / 2};
        }
        int s = n == 15 ? 3 : (n == 19 ? 3 : 2);
        return new int[]{s, n / 2, n - 1 - s};
    }
}
