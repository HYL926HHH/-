package com.gomoku.puzzle;

import com.gomoku.model.Board;
import com.gomoku.model.Pos;
import com.gomoku.model.Stone;

public final class Puzzle {
    public final String id;
    public final String title;
    public final String goal;
    public final int size;
    public final Stone[][] setup;
    public final Stone toMove;
    public final Pos solution;
    public final String comment;

    public Puzzle(String id, String title, String goal, int size, Stone[][] setup, Stone toMove, Pos solution, String comment) {
        this.id = id;
        this.title = title;
        this.goal = goal;
        this.size = size;
        this.setup = setup;
        this.toMove = toMove;
        this.solution = solution;
        this.comment = comment;
    }

    public void applyTo(Board board) {
        board.clear();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (setup[y][x] != null) {
                    board.set(x, y, setup[y][x]);
                }
            }
        }
    }

    public static Stone[][] empty(int size) {
        return new Stone[size][size];
    }

    @Override
    public String toString() {
        return title;
    }
}
