package com.gomoku.ai;

import com.gomoku.model.Board;
import com.gomoku.model.ForbiddenAnalyzer;
import com.gomoku.model.Stone;

public final class Evaluator {
    public static final int LIVE_FOUR = 50_000;
    public static final int DEAD_FOUR = 8_000;
    public static final int LIVE_THREE = 4_000;
    public static final int DEAD_THREE = 400;
    public static final int LIVE_TWO = 80;

    private Evaluator() {
    }

    public static int score(Board board, Stone stone) {
        int total = 0;
        int n = board.size();
        boolean[][] seen = new boolean[n][n];
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                if (board.get(x, y) != stone || seen[y][x]) {
                    continue;
                }
                for (int[] d : ForbiddenAnalyzer.DIRS) {
                    int len = ForbiddenAnalyzer.lineLength(board, x, y, stone, d[0], d[1]);
                    int open = openEnds(board, x, y, stone, d[0], d[1]);
                    if (len >= 5) {
                        total += 200_000;
                    } else if (len == 4 && open == 2) {
                        total += LIVE_FOUR;
                    } else if (len == 4 && open == 1) {
                        total += DEAD_FOUR;
                    } else if (len == 3 && open == 2) {
                        total += LIVE_THREE;
                    } else if (len == 3 && open == 1) {
                        total += DEAD_THREE;
                    } else if (len == 2 && open == 2) {
                        total += LIVE_TWO;
                    }
                }
            }
        }
        return total;
    }

    public static int patternGain(Board board, int x, int y, Stone stone) {
        int s = 0;
        for (int[] d : ForbiddenAnalyzer.DIRS) {
            int len = ForbiddenAnalyzer.lineLength(board, x, y, stone, d[0], d[1]);
            int open = openEnds(board, x, y, stone, d[0], d[1]);
            if (len >= 5) {
                s += 200_000;
            } else if (len == 4 && open == 2) {
                s += LIVE_FOUR;
            } else if (len == 4 && open == 1) {
                s += DEAD_FOUR;
            } else if (len == 3 && open == 2) {
                s += LIVE_THREE;
            } else if (len == 3 && open == 1) {
                s += DEAD_THREE;
            } else if (len == 2 && open == 2) {
                s += LIVE_TWO;
            }
        }
        return s;
    }

    static int openEnds(Board board, int x, int y, Stone stone, int dx, int dy) {
        int open = 0;
        int fx = x;
        int fy = y;
        while (board.inBounds(fx - dx, fy - dy) && board.get(fx - dx, fy - dy) == stone) {
            fx -= dx;
            fy -= dy;
        }
        int lx = x;
        int ly = y;
        while (board.inBounds(lx + dx, ly + dy) && board.get(lx + dx, ly + dy) == stone) {
            lx += dx;
            ly += dy;
        }
        if (board.inBounds(fx - dx, fy - dy) && board.get(fx - dx, fy - dy) == null) {
            open++;
        }
        if (board.inBounds(lx + dx, ly + dy) && board.get(lx + dx, ly + dy) == null) {
            open++;
        }
        return open;
    }
}
