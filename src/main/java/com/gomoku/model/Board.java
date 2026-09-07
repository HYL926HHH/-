package com.gomoku.model;

import java.util.Arrays;

public final class Board {
    private final int size;
    private final Stone[][] cells;

    public Board(int size) {
        if (size < 9 || size > 19) {
            throw new IllegalArgumentException("棋盘大小需在 9 到 19 之间");
        }
        this.size = size;
        this.cells = new Stone[size][size];
    }

    public Board(Board other) {
        this.size = other.size;
        this.cells = new Stone[size][size];
        for (int i = 0; i < size; i++) {
            this.cells[i] = Arrays.copyOf(other.cells[i], size);
        }
    }

    public int size() {
        return size;
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < size && y < size;
    }

    public Stone get(int x, int y) {
        return cells[y][x];
    }

    public void set(int x, int y, Stone stone) {
        cells[y][x] = stone;
    }

    public boolean isEmpty(int x, int y) {
        return inBounds(x, y) && cells[y][x] == null;
    }

    public int emptyCount() {
        int n = 0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (cells[y][x] == null) {
                    n++;
                }
            }
        }
        return n;
    }

    public boolean isFull() {
        return emptyCount() == 0;
    }

    public void clear() {
        for (int y = 0; y < size; y++) {
            Arrays.fill(cells[y], null);
        }
    }
}
