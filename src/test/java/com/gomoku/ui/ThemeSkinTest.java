package com.gomoku.ui;

import com.gomoku.model.Game;
import com.gomoku.model.GameSettings;
import com.gomoku.persist.AppStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThemeSkinTest {
    static {
        System.setProperty("java.awt.headless", "true");
    }
    @Test
    void featuredStylesAreGufengCartoonModern() {
        Theme[] featured = Theme.featured();
        assertEquals(3, featured.length);
        assertEquals("gufeng", featured[0].id);
        assertEquals("cartoon", featured[1].id);
        assertEquals("modern", featured[2].id);
        assertEquals("古风", featured[0].label);
        assertEquals("卡通", featured[1].label);
        assertEquals("现代", featured[2].label);
    }

    @Test
    void byIdFallsBackToGufeng() {
        assertEquals("gufeng", Theme.byId(null).id);
        assertEquals("gufeng", Theme.byId("unknown").id);
        assertEquals("cartoon", Theme.byId("cartoon").id);
        assertEquals("modern", Theme.byId("modern").id);
        assertTrue(Theme.isKnown("gufeng"));
        assertTrue(!Theme.isKnown("nope"));
    }

    @Test
    void eachStyleHasMatchingDefaultStone() {
        assertEquals("jade", Theme.gufeng().defaultSkinId);
        assertEquals("candy", Theme.cartoon().defaultSkinId);
        assertEquals("glass", Theme.modern().defaultSkinId);
        assertEquals(StoneSkin.JADE, StoneSkin.byId("jade"));
        assertEquals(StoneSkin.CANDY, StoneSkin.byId("candy"));
        assertEquals(StoneSkin.GLASS, StoneSkin.byId("glass"));
    }

    @Test
    void stylesAreVisuallyDistinct() {
        Theme a = Theme.gufeng();
        Theme b = Theme.cartoon();
        Theme c = Theme.modern();
        assertNotEquals(a.board, b.board);
        assertNotEquals(b.panelBg, c.panelBg);
        assertNotEquals(a.family, b.family);
        assertNotEquals(b.family, c.family);
        assertNotNull(a.tagline);
        assertNotNull(b.tagline);
        assertNotNull(c.tagline);
    }

    @Test
    void boardPanelPaintsEverySkinHeadless() {
        GameSettings settings = new GameSettings();
        settings.countdownEnabled = false;
        Game game = new Game(settings);
        game.commit(7, 7);
        game.commit(8, 7);
        BoardPanel panel = new BoardPanel();
        panel.setSize(640, 640);
        panel.setGame(game);
        BufferedImage img = new BufferedImage(640, 640, BufferedImage.TYPE_INT_ARGB);
        for (Theme theme : Theme.all()) {
            panel.applyLook(theme, StoneSkin.byId(theme.defaultSkinId));
            var g = img.createGraphics();
            panel.paint(g);
            g.dispose();
            assertNotEquals(0, img.getRGB(320, 320));
        }
    }

    @Test
    void lookPrefsRoundTrip(@TempDir Path dir) throws Exception {
        AppStore store = new AppStore(dir);
        store.saveLook("cartoon", "candy");
        String[] look = store.loadLook();
        assertNotNull(look);
        assertEquals("cartoon", look[0]);
        assertEquals("candy", look[1]);
    }
}
