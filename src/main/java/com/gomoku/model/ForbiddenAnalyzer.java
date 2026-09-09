package com.gomoku.model;

/**
 * 连珠禁手：黑棋长连、双三、双四为禁；恰好五连为胜。白棋无禁手，长连也胜。
 */
public final class ForbiddenAnalyzer {
    public static final int[][] DIRS = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

    private ForbiddenAnalyzer() {
    }

    public static int lineLength(Board board, int x, int y, Stone stone, int dx, int dy) {
        int n = 1;
        for (int s = 1; s < board.size(); s++) {
            int nx = x + dx * s;
            int ny = y + dy * s;
            if (!board.inBounds(nx, ny) || board.get(nx, ny) != stone) {
                break;
            }
            n++;
        }
        for (int s = 1; s < board.size(); s++) {
            int nx = x - dx * s;
            int ny = y - dy * s;
            if (!board.inBounds(nx, ny) || board.get(nx, ny) != stone) {
                break;
            }
            n++;
        }
        return n;
    }

    public static boolean isFiveOrMore(Board board, int x, int y, Stone stone) {
        for (int[] d : DIRS) {
            if (lineLength(board, x, y, stone, d[0], d[1]) >= 5) {
                return true;
            }
        }
        return false;
    }

    public static boolean isExactFive(Board board, int x, int y, Stone stone) {
        for (int[] d : DIRS) {
            int len = lineLength(board, x, y, stone, d[0], d[1]);
            if (len == 5) {
                return true;
            }
            if (len > 5 && stone == Stone.WHITE) {
                return true;
            }
        }
        return false;
    }

    public static ForbiddenKind classifyBlack(Board board, int x, int y) {
        if (!board.inBounds(x, y) || board.get(x, y) != Stone.BLACK) {
            return ForbiddenKind.NONE;
        }
        boolean overline = false;
        boolean exactFive = false;
        int fours = 0;
        int liveThrees = 0;
        for (int[] d : DIRS) {
            int len = lineLength(board, x, y, Stone.BLACK, d[0], d[1]);
            if (len > 5) {
                overline = true;
            } else if (len == 5) {
                exactFive = true;
            }
            fours += countFoursOnAxis(board, x, y, d[0], d[1]);
            liveThrees += countLiveThreesOnAxis(board, x, y, d[0], d[1]);
        }
        if (exactFive) {
            return ForbiddenKind.NONE;
        }
        if (overline) {
            return ForbiddenKind.OVERLINE;
        }
        if (fours >= 2) {
            return ForbiddenKind.DOUBLE_FOUR;
        }
        if (liveThrees >= 2) {
            return ForbiddenKind.DOUBLE_THREE;
        }
        return ForbiddenKind.NONE;
    }

    /**
     * 该轴上，落子点参与的“四”（再下一手即可成五）数量，0 或 1。
     */
    static int countFoursOnAxis(Board board, int x, int y, int dx, int dy) {
        int threats = 0;
        for (int i = -5; i <= 0; i++) {
            int empties = 0;
            int blacks = 0;
            boolean includes = false;
            boolean blocked = false;
            int emptyX = -1;
            int emptyY = -1;
            for (int k = 0; k < 5; k++) {
                int nx = x + (i + k) * dx;
                int ny = y + (i + k) * dy;
                if (!board.inBounds(nx, ny)) {
                    blocked = true;
                    break;
                }
                Stone s = board.get(nx, ny);
                if (s == Stone.WHITE) {
                    blocked = true;
                    break;
                }
                if (s == Stone.BLACK) {
                    blacks++;
                    if (nx == x && ny == y) {
                        includes = true;
                    }
                } else {
                    empties++;
                    emptyX = nx;
                    emptyY = ny;
                }
            }
            if (blocked || !includes || blacks != 4 || empties != 1) {
                continue;
            }
            if (wouldBeOverlineIfFilled(board, emptyX, emptyY)) {
                continue;
            }
            threats++;
        }
        return threats > 0 ? 1 : 0;
    }

    private static boolean wouldBeOverlineIfFilled(Board board, int x, int y) {
        board.set(x, y, Stone.BLACK);
        boolean over = false;
        for (int[] d : DIRS) {
            if (lineLength(board, x, y, Stone.BLACK, d[0], d[1]) > 5) {
                over = true;
                break;
            }
        }
        board.set(x, y, null);
        return over;
    }

    /**
     * 活三：存在一个空点，黑落该点后形成活四（两端可延伸的四）。
     */
    static int countLiveThreesOnAxis(Board board, int x, int y, int dx, int dy) {
        for (int i = -4; i <= 4; i++) {
            int nx = x + i * dx;
            int ny = y + i * dy;
            if (!board.inBounds(nx, ny) || board.get(nx, ny) != null) {
                continue;
            }
            board.set(nx, ny, Stone.BLACK);
            boolean liveFour = isLiveFourOnAxis(board, nx, ny, dx, dy);
            int len = lineLength(board, nx, ny, Stone.BLACK, dx, dy);
            board.set(nx, ny, null);
            if (liveFour && len == 4) {
                return 1;
            }
        }
        return 0;
    }

    static boolean isLiveFourOnAxis(Board board, int x, int y, int dx, int dy) {
        int len = lineLength(board, x, y, Stone.BLACK, dx, dy);
        if (len != 4) {
            return jumpLiveFour(board, x, y, dx, dy);
        }
        int fx = x;
        int fy = y;
        while (board.inBounds(fx - dx, fy - dy) && board.get(fx - dx, fy - dy) == Stone.BLACK) {
            fx -= dx;
            fy -= dy;
        }
        int lx = x;
        int ly = y;
        while (board.inBounds(lx + dx, ly + dy) && board.get(lx + dx, ly + dy) == Stone.BLACK) {
            lx += dx;
            ly += dy;
        }
        int bx = fx - dx;
        int by = fy - dy;
        int ax = lx + dx;
        int ay = ly + dy;
        boolean openBefore = board.inBounds(bx, by) && board.get(bx, by) == null;
        boolean openAfter = board.inBounds(ax, ay) && board.get(ax, ay) == null;
        return openBefore && openAfter;
    }

    private static boolean jumpLiveFour(Board board, int x, int y, int dx, int dy) {
        // .BBB.B. / .BB.BB. patterns that still have two winning points
        int wins = 0;
        for (int i = -5; i <= 5; i++) {
            int nx = x + i * dx;
            int ny = y + i * dy;
            if (!board.inBounds(nx, ny) || board.get(nx, ny) != null) {
                continue;
            }
            board.set(nx, ny, Stone.BLACK);
            if (lineLength(board, nx, ny, Stone.BLACK, dx, dy) == 5) {
                wins++;
            }
            board.set(nx, ny, null);
        }
        return wins >= 2;
    }

    public static boolean isForbiddenMove(Board board, int x, int y, boolean renju) {
        if (!renju) {
            return false;
        }
        if (!board.isEmpty(x, y)) {
            return true;
        }
        board.set(x, y, Stone.BLACK);
        ForbiddenKind kind = classifyBlack(board, x, y);
        board.set(x, y, null);
        return kind != ForbiddenKind.NONE;
    }

    public static ForbiddenKind previewForbidden(Board board, int x, int y, boolean renju) {
        if (!renju || !board.isEmpty(x, y)) {
            return ForbiddenKind.NONE;
        }
        board.set(x, y, Stone.BLACK);
        ForbiddenKind kind = classifyBlack(board, x, y);
        board.set(x, y, null);
        return kind;
    }
}
