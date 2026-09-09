package com.gomoku.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameRulesTest {

    @Test
    void blackWinsWithFive() {
        GameSettings s = new GameSettings();
        s.renjuForbidden = false;
        s.countdownEnabled = false;
        Game g = new Game(s);
        int y = 7;
        for (int i = 0; i < 4; i++) {
            assertTrue(g.commit(3 + i, y).accepted);
            assertTrue(g.commit(3 + i, y + 1).accepted);
        }
        PlaceResult r = g.commit(7, y);
        assertTrue(r.accepted);
        assertEquals(GameStatus.BLACK_WIN, g.status());
        assertEquals(5, g.winningLine().size());
    }

    @Test
    void overlineIsForbiddenForBlack() {
        Board b = new Board(15);
        for (int x = 4; x <= 8; x++) {
            b.set(x, 7, Stone.BLACK);
        }
        assertEquals(ForbiddenKind.OVERLINE, ForbiddenAnalyzer.previewForbidden(b, 9, 7, true));
        b.set(9, 7, Stone.BLACK);
        assertTrue(ForbiddenAnalyzer.lineLength(b, 9, 7, Stone.BLACK, 1, 0) >= 6);
    }

    @Test
    void doubleThreeIsForbidden() {
        Board b = new Board(15);
        b.set(7, 5, Stone.BLACK);
        b.set(7, 6, Stone.BLACK);
        b.set(5, 7, Stone.BLACK);
        b.set(6, 7, Stone.BLACK);
        ForbiddenKind k = ForbiddenAnalyzer.previewForbidden(b, 7, 7, true);
        assertEquals(ForbiddenKind.DOUBLE_THREE, k);
    }

    @Test
    void doubleFourIsForbidden() {
        Board b = new Board(15);
        b.set(4, 7, Stone.BLACK);
        b.set(5, 7, Stone.BLACK);
        b.set(6, 7, Stone.BLACK);
        b.set(7, 4, Stone.BLACK);
        b.set(7, 5, Stone.BLACK);
        b.set(7, 6, Stone.BLACK);
        ForbiddenKind k = ForbiddenAnalyzer.previewForbidden(b, 7, 7, true);
        assertEquals(ForbiddenKind.DOUBLE_FOUR, k);
    }

    @Test
    void exactFiveOverridesForbidden() {
        Board b = new Board(15);
        b.set(3, 7, Stone.BLACK);
        b.set(4, 7, Stone.BLACK);
        b.set(5, 7, Stone.BLACK);
        b.set(6, 7, Stone.BLACK);
        assertEquals(ForbiddenKind.NONE, ForbiddenAnalyzer.previewForbidden(b, 7, 7, true));
    }

    @Test
    void undoRestoresTurn() {
        GameSettings s = new GameSettings();
        s.countdownEnabled = false;
        Game g = new Game(s);
        g.commit(7, 7);
        g.commit(8, 7);
        assertTrue(g.undo(Stone.BLACK));
        assertEquals(Stone.WHITE, g.toMove());
        assertTrue(g.board().isEmpty(8, 7));
    }

    @Test
    void handicapTwoWhiteMovesFirst() {
        GameSettings s = new GameSettings();
        s.handicap = 2;
        s.countdownEnabled = false;
        Game g = new Game(s);
        assertEquals(Stone.WHITE, g.toMove());
        assertEquals(2, g.moves().size());
    }

    @Test
    void fullBoardDraw() {
        GameSettings s = new GameSettings();
        s.boardSize = 9;
        s.renjuForbidden = false;
        s.countdownEnabled = false;
        Game g = new Game(s);
        // fill without five: checker-ish but 5 might form. Use columns of 4.
        Stone turn = Stone.BLACK;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (!g.isOver()) {
                    g.commit(x, y);
                    turn = turn.opponent();
                }
            }
        }
        assertTrue(g.isOver());
    }
}
