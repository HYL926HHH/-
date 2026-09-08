package com.gomoku.demo;

import com.gomoku.model.Game;
import com.gomoku.model.GameSettings;
import com.gomoku.ui.BoardPanel;
import com.gomoku.ui.StoneSkin;
import com.gomoku.ui.StyleBar;
import com.gomoku.ui.Theme;

import javax.imageio.ImageIO;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SkinPreview {
    public static void main(String[] args) throws Exception {
        Path out = Path.of(args.length > 0 ? args[0] : "target/skin-previews");
        Files.createDirectories(out);
        String only = args.length > 1 ? args[1] : null;
        GameSettings settings = new GameSettings();
        settings.countdownEnabled = false;
        settings.animation = false;
        settings.effects = false;
        Game game = new Game(settings);
        int[][] opening = {{7, 7}, {8, 7}, {6, 8}, {7, 8}, {8, 6}, {5, 7}, {7, 6}, {7, 9}};
        for (int[] p : opening) {
            game.commit(p[0], p[1]);
        }
        for (Theme theme : Theme.featured()) {
            if (only != null && !only.equals(theme.id)) {
                continue;
            }
            render(out, game, theme);
            System.out.println(out.resolve(theme.id + ".png").toAbsolutePath());
        }
        System.exit(0);
    }

    private static void render(Path out, Game game, Theme theme) throws Exception {
        BoardPanel board = new BoardPanel();
        board.setGame(game);
        board.applyLook(theme, StoneSkin.byId(theme.defaultSkinId));
        StyleBar bar = new StyleBar(t -> {
        });
        bar.setTheme(theme);
        int width = 980;
        int barH = 108;
        int boardH = 720;
        bar.setSize(width, barH);
        layoutTree(bar);
        board.setSize(width, boardH);
        BufferedImage img = new BufferedImage(width, barH + boardH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(theme.panelBg);
        g.fillRect(0, 0, width, barH + boardH);
        bar.paint(g);
        g.translate(0, barH);
        board.paint(g);
        g.dispose();
        ImageIO.write(img, "png", out.resolve(theme.id + ".png").toFile());
    }

    private static void layoutTree(Component c) {
        c.setPreferredSize(new Dimension(Math.max(1, c.getWidth()), Math.max(1, c.getHeight())));
        if (c instanceof Container box) {
            box.doLayout();
            for (Component child : box.getComponents()) {
                layoutTree(child);
            }
        }
    }
}
