package com.gomoku.ai;

import com.gomoku.model.Game;
import com.gomoku.model.GameSettings;
import com.gomoku.model.Stone;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GomokuAiTest {

    @Test
    void blocksOpenFour() {
        GameSettings s = new GameSettings();
        s.countdownEnabled = false;
        s.renjuForbidden = false;
        Game g = new Game(s);
        // black 4 in a row horizontally with open end
        g.commit(4, 7);
        g.commit(0, 0);
        g.commit(5, 7);
        g.commit(0, 1);
        g.commit(6, 7);
        g.commit(0, 2);
        g.commit(7, 7);
        // white to move should block 3 or 8
        GomokuAi ai = new GomokuAi();
        var p = ai.choose(g);
        assertNotNull(p);
        assertEquals(7, p.y);
        assertEquals(true, p.x == 3 || p.x == 8);
    }

    @Test
    void takesWinningFive() {
        GameSettings s = new GameSettings();
        s.countdownEnabled = false;
        s.renjuForbidden = false;
        Game g = new Game(s);
        g.commit(4, 7);
        g.commit(0, 0);
        g.commit(5, 7);
        g.commit(0, 1);
        g.commit(6, 7);
        g.commit(0, 2);
        g.commit(7, 7);
        g.commit(1, 0);
        // black can win at 3 or 8
        var p = new GomokuAi().choose(g);
        assertEquals(7, p.y);
        assertEquals(true, p.x == 3 || p.x == 8);
        g.commit(p.x, p.y);
        assertEquals(com.gomoku.model.GameStatus.BLACK_WIN, g.status());
    }

    @Test
    void openingBookCenter() {
        GameSettings s = new GameSettings();
        s.aiLevel = com.gomoku.model.AiLevel.MASTER;
        s.countdownEnabled = false;
        Game g = new Game(s);
        var p = new GomokuAi().choose(g);
        assertEquals(7, p.x);
        assertEquals(7, p.y);
    }
}
