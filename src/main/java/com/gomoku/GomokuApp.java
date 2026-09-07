package com.gomoku;

import com.gomoku.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class GomokuApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
