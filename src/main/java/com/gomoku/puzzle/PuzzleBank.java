package com.gomoku.puzzle;

import com.gomoku.model.Pos;
import com.gomoku.model.Stone;

import java.util.List;

public final class PuzzleBank {
    private PuzzleBank() {
    }

    public static List<Puzzle> all() {
        return List.of(liveFourKill(), blockFour(), forkThree());
    }

    private static Puzzle liveFourKill() {
        int n = 15;
        Stone[][] b = Puzzle.empty(n);
        // 白有活四，黑需立即堵住
        b[7][4] = Stone.WHITE;
        b[7][5] = Stone.WHITE;
        b[7][6] = Stone.WHITE;
        b[7][7] = Stone.WHITE;
        b[8][8] = Stone.BLACK;
        b[6][8] = Stone.BLACK;
        b[5][8] = Stone.BLACK;
        return new Puzzle("p1", "死活题：堵活四", "黑先，唯一正确点堵住白棋活四", n, b, Stone.BLACK,
                new Pos(8, 7), "白横线已成 .WWWW. 形态，必须落在两端之一；本题指定右侧 H8。");
    }

    private static Puzzle blockFour() {
        int n = 15;
        Stone[][] b = Puzzle.empty(n);
        b[7][7] = Stone.BLACK;
        b[7][8] = Stone.BLACK;
        b[7][9] = Stone.BLACK;
        b[7][10] = Stone.BLACK;
        b[6][7] = Stone.WHITE;
        b[8][10] = Stone.WHITE;
        return new Puzzle("p2", "残局：冲四取胜", "黑先，冲四成五", n, b, Stone.BLACK,
                new Pos(6, 7), "横线四连，左边 G8 成五。");
    }

    private static Puzzle forkThree() {
        int n = 15;
        Stone[][] b = Puzzle.empty(n);
        b[7][7] = Stone.BLACK;
        b[7][8] = Stone.BLACK;
        b[6][7] = Stone.BLACK;
        b[5][7] = Stone.WHITE;
        b[7][6] = Stone.WHITE;
        b[8][8] = Stone.WHITE;
        return new Puzzle("p3", "禁手演示铺垫：活三选择", "黑先走活三", n, b, Stone.BLACK,
                new Pos(7, 9), "续走 J8 形成横线活三。");
    }
}
