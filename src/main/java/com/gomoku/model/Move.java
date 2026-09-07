package com.gomoku.model;

public final class Move {
    public final int x;
    public final int y;
    public final Stone stone;
    public final long atMillis;
    public String annotation;
    public boolean forbiddenHint;

    public Move(int x, int y, Stone stone, long atMillis) {
        this.x = x;
        this.y = y;
        this.stone = stone;
        this.atMillis = atMillis;
    }

    public Pos pos() {
        return new Pos(x, y);
    }

    public String algebraic() {
        return Pos.toAlgebraic(x, y);
    }
}
