package com.gomoku.ai;

import com.gomoku.model.AiLevel;
import com.gomoku.model.Game;
import com.gomoku.model.GameMode;
import com.gomoku.model.GameSettings;
import com.gomoku.model.GameStatus;

import java.util.LinkedHashMap;
import java.util.Map;

/** 极简自对弈统计，便于观察不同难度胜率。 */
public final class SelfPlayTrainer {
    public Map<String, Integer> run(int games, AiLevel black, AiLevel white) {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("black", 0);
        stats.put("white", 0);
        stats.put("draw", 0);
        GomokuAi ai = new GomokuAi();
        for (int i = 0; i < games; i++) {
            GameSettings s = new GameSettings();
            s.mode = GameMode.PVE;
            s.countdownEnabled = false;
            s.renjuForbidden = true;
            Game g = new Game(s);
            int guard = 0;
            while (!g.isOver() && guard++ < s.boardSize * s.boardSize) {
                s.aiLevel = g.toMove() == com.gomoku.model.Stone.BLACK ? black : white;
                var p = ai.choose(g);
                g.commit(p.x, p.y);
            }
            if (g.status() == GameStatus.BLACK_WIN) {
                stats.put("black", stats.get("black") + 1);
            } else if (g.status() == GameStatus.WHITE_WIN) {
                stats.put("white", stats.get("white") + 1);
            } else {
                stats.put("draw", stats.get("draw") + 1);
            }
        }
        return stats;
    }
}
