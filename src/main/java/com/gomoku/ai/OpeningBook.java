package com.gomoku.ai;

import com.gomoku.model.Game;
import com.gomoku.model.Move;
import com.gomoku.model.Pos;

import java.util.List;

public final class OpeningBook {
    public Pos suggest(Game game) {
        List<Move> moves = game.moves();
        int n = game.board().size();
        int c = n / 2;
        if (moves.isEmpty()) {
            return new Pos(c, c);
        }
        if (moves.size() == 1) {
            Move m = moves.get(0);
            if (m.x == c && m.y == c) {
                return new Pos(c + 1, c);
            }
            return new Pos(c, c);
        }
        if (moves.size() == 2) {
            return new Pos(c + 1, c + 1);
        }
        return null;
    }
}
