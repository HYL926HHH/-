package com.gomoku.model;

import java.util.Objects;

public final class Pos {
    public final int x;
    public final int y;

    public Pos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pos pos)) {
            return false;
        }
        return x == pos.x && y == pos.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return toAlgebraic(x, y);
    }

    public static String toAlgebraic(int x, int y) {
        return String.valueOf((char) ('A' + x)) + (y + 1);
    }
}
