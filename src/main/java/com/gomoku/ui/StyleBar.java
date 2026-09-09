package com.gomoku.ui;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class StyleBar extends JPanel {
    private final Consumer<Theme> onPick;
    private final List<Card> cards = new ArrayList<>();
    private String selectedId;

    public StyleBar(Consumer<Theme> onPick) {
        this.onPick = onPick;
        setOpaque(true);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 12, 8, 12));
        setPreferredSize(new Dimension(1100, 108));
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);
        for (Theme theme : Theme.featured()) {
            Card card = new Card(theme);
            cards.add(card);
            row.add(card);
        }
        add(row, BorderLayout.CENTER);
    }

    public void setTheme(Theme theme) {
        this.selectedId = theme.id;
        setBackground(theme.panelBg);
        for (Card card : cards) {
            card.refresh();
        }
        repaint();
    }

    private final class Card extends JPanel {
        private final Theme preview;
        private boolean hover;

        Card(Theme preview) {
            this.preview = preview;
            setPreferredSize(new Dimension(220, 86));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setOpaque(false);
            MouseAdapter mouse = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onPick.accept(preview);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            };
            addMouseListener(mouse);
        }

        void refresh() {
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean on = preview.id.equals(selectedId);
            int w = getWidth() - 1;
            int h = getHeight() - 1;
            g2.setColor(new Color(0, 0, 0, on || hover ? 40 : 18));
            g2.fillRoundRect(3, 5, w, h, 16, 16);
            g2.setPaint(new GradientPaint(0, 0, preview.cardBg, w, h, preview.panelBg));
            g2.fillRoundRect(0, 0, w, h, 16, 16);
            g2.setColor(on ? preview.accent : hover ? preview.frameTrim : preview.frame);
            g2.setStroke(new java.awt.BasicStroke(on ? 2.6f : 1.2f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, 16, 16);
            paintMiniBoard(g2, 12, 14, 58, preview);
            g2.setFont(Fonts.title(preview, 18f));
            g2.setColor(preview.titleFg);
            g2.drawString(preview.label, 82, 34);
            g2.setFont(Fonts.ui(preview, 11f, Font.PLAIN));
            g2.setColor(preview.panelMuted);
            g2.drawString(preview.tagline, 82, 54);
            if (on) {
                g2.setColor(preview.accent);
                g2.fillRoundRect(82, 64, 46, 6, 6, 6);
            }
            g2.dispose();
        }

        private void paintMiniBoard(Graphics2D g2, int x, int y, int s, Theme t) {
            g2.setColor(t.frame);
            g2.fillRoundRect(x, y, s, s, 10, 10);
            g2.setPaint(new GradientPaint(x, y, t.board, x + s, y + s, t.boardAlt));
            g2.fillRoundRect(x + 6, y + 6, s - 12, s - 12, 6, 6);
            g2.setColor(t.line);
            int inner = s - 16;
            int ox = x + 8;
            int oy = y + 8;
            for (int i = 0; i < 4; i++) {
                int p = ox + i * inner / 3;
                g2.drawLine(ox, oy + i * inner / 3, ox + inner, oy + i * inner / 3);
                g2.drawLine(p, oy, p, oy + inner);
            }
            g2.setColor(new Color(0x1A1A1A));
            g2.fillOval(ox + 10, oy + 10, 10, 10);
            g2.setColor(Color.WHITE);
            g2.fillOval(ox + inner - 18, oy + inner - 18, 10, 10);
        }
    }
}
