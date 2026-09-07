package com.gomoku.persist;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameRecordTest {
    @Test
    void roundTrip() {
        GameRecord r = new GameRecord();
        r.blackName = "甲";
        r.whiteName = "乙";
        r.moves.add(new com.gomoku.model.Move(7, 7, com.gomoku.model.Stone.BLACK, 1));
        r.moves.get(0).annotation = "天元";
        GameRecord p = GameRecord.parse(r.toText());
        assertEquals("甲", p.blackName);
        assertEquals(7, p.moves.get(0).x);
        assertEquals("天元", p.moves.get(0).annotation);
    }

    @Test
    void storeLeaderboard(@TempDir Path dir) throws Exception {
        AppStore store = new AppStore(dir);
        store.recordResult("甲", true, false);
        store.recordResult("乙", false, false);
        assertEquals(1, store.loadRatings().get(0).wins);
        assertTrue(store.loadRatings().get(0).elo > 1200);
    }
}
