package com.gomoku.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

final class Chrome {
    private Chrome() {
    }

    static void apply(Container root, Theme theme) {
        applyOne(root, theme);
        if (root instanceof JMenuBar bar) {
            for (int i = 0; i < bar.getMenuCount(); i++) {
                JMenu menu = bar.getMenu(i);
                if (menu != null) {
                    applyMenu(menu, theme);
                }
            }
            return;
        }
        for (Component child : root.getComponents()) {
            if (child instanceof BoardPanel || child instanceof StyleBar) {
                continue;
            }
            if (child instanceof Container c) {
                apply(c, theme);
            } else {
                applyOne(child, theme);
            }
        }
    }

    private static void applyMenu(JMenu menu, Theme theme) {
        menu.setForeground(theme.panelFg);
        menu.setFont(Fonts.ui(theme, 13f, Font.PLAIN));
        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem item = menu.getItem(i);
            if (item == null) {
                continue;
            }
            item.setBackground(theme.cardBg);
            item.setForeground(theme.panelFg);
            item.setFont(Fonts.ui(theme, 13f, Font.PLAIN));
            item.setOpaque(true);
        }
    }

    private static void applyOne(Component c, Theme theme) {
        Font ui = Fonts.ui(theme, 13f, Font.PLAIN);
        if (c instanceof JMenuBar bar) {
            bar.setBackground(theme.panelBg);
            bar.setForeground(theme.panelFg);
            bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, theme.frameTrim));
            bar.setOpaque(true);
            return;
        }
        if (c instanceof JButton b) {
            b.setBackground(theme.buttonBg);
            b.setForeground(theme.buttonFg);
            b.setFont(Fonts.ui(theme, 13f, Font.BOLD));
            b.setFocusPainted(false);
            b.setOpaque(true);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(theme.accentSoft, 1),
                    new EmptyBorder(5, 8, 5, 8)));
            return;
        }
        if (c instanceof JCheckBox box) {
            box.setBackground(theme.panelBg);
            box.setForeground(theme.panelFg);
            box.setFont(ui);
            box.setOpaque(true);
            return;
        }
        if (c instanceof JLabel label) {
            label.setForeground(theme.panelFg);
            label.setFont(ui);
            return;
        }
        if (c instanceof JTextArea area) {
            area.setBackground(mix(theme.cardBg, theme.panelBg));
            area.setForeground(theme.panelFg);
            area.setCaretColor(theme.accent);
            area.setFont(ui);
            area.setBorder(new EmptyBorder(6, 6, 6, 6));
            return;
        }
        if (c instanceof JTextField field) {
            field.setBackground(theme.cardBg);
            field.setForeground(theme.panelFg);
            field.setCaretColor(theme.accent);
            field.setFont(ui);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(theme.frameTrim, 1),
                    new EmptyBorder(4, 6, 4, 6)));
            return;
        }
        if (c instanceof JList<?> list) {
            list.setBackground(theme.cardBg);
            list.setForeground(theme.panelFg);
            list.setSelectionBackground(theme.accent);
            list.setSelectionForeground(theme.buttonFg);
            list.setFont(ui);
            return;
        }
        if (c instanceof JComboBox<?> box) {
            box.setBackground(theme.cardBg);
            box.setForeground(theme.panelFg);
            box.setFont(ui);
            return;
        }
        if (c instanceof JSpinner spinner) {
            spinner.setBackground(theme.cardBg);
            spinner.setForeground(theme.panelFg);
            spinner.setFont(ui);
            return;
        }
        if (c instanceof JScrollPane scroll) {
            scroll.setBackground(theme.panelBg);
            scroll.setBorder(BorderFactory.createLineBorder(theme.frame, 1));
            JViewport vp = scroll.getViewport();
            if (vp != null) {
                vp.setBackground(theme.cardBg);
            }
            return;
        }
        if (c instanceof JSplitPane split) {
            split.setBackground(theme.panelBg);
            split.setBorder(null);
            return;
        }
        if (c instanceof JPanel panel) {
            panel.setBackground(theme.panelBg);
            if (panel.getBorder() == null || panel.getBorder() instanceof EmptyBorder) {
                // keep padding
            }
            return;
        }
        if (c instanceof JComponent jc) {
            jc.setBackground(theme.panelBg);
            jc.setForeground(theme.panelFg);
        } else {
            c.setBackground(theme.panelBg);
            c.setForeground(theme.panelFg);
        }
    }

    private static Color mix(Color a, Color b) {
        return new Color(
                (a.getRed() * 2 + b.getRed()) / 3,
                (a.getGreen() * 2 + b.getGreen()) / 3,
                (a.getBlue() * 2 + b.getBlue()) / 3);
    }
}
