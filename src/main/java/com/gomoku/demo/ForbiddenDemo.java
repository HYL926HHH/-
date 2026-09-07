package com.gomoku.demo;

import com.gomoku.model.Board;
import com.gomoku.model.ForbiddenAnalyzer;
import com.gomoku.model.ForbiddenKind;
import com.gomoku.model.Pos;
import com.gomoku.model.Stone;

import java.util.ArrayList;
import java.util.List;

public final class ForbiddenDemo {
    public record Example(String title, String explain, Board board, Pos mark, ForbiddenKind kind) {
    }

    public static List<Example> examples() {
        List<Example> list = new ArrayList<>();
        list.add(overline());
        list.add(doubleThree());
        list.add(doubleFour());
        return list;
    }

    private static Example overline() {
        Board b = new Board(15);
        for (int x = 5; x <= 9; x++) {
            b.set(x, 7, Stone.BLACK);
        }
        b.set(10, 7, Stone.BLACK);
        ForbiddenKind k = ForbiddenAnalyzer.classifyBlack(b, 10, 7);
        return new Example("长连", "黑棋一方向出现 6 子连珠，为禁手（白棋长连仍算胜）。", b, new Pos(10, 7), k);
    }

    private static Example doubleThree() {
        Board b = new Board(15);
        b.set(7, 5, Stone.BLACK);
        b.set(7, 6, Stone.BLACK);
        b.set(5, 7, Stone.BLACK);
        b.set(6, 7, Stone.BLACK);
        b.set(7, 7, Stone.BLACK);
        ForbiddenKind k = ForbiddenAnalyzer.classifyBlack(b, 7, 7);
        return new Example("双三", "一子同时形成两个活三（横 + 竖）。", b, new Pos(7, 7), k);
    }

    private static Example doubleFour() {
        Board b = new Board(15);
        b.set(4, 7, Stone.BLACK);
        b.set(5, 7, Stone.BLACK);
        b.set(6, 7, Stone.BLACK);
        b.set(7, 4, Stone.BLACK);
        b.set(7, 5, Stone.BLACK);
        b.set(7, 6, Stone.BLACK);
        b.set(7, 7, Stone.BLACK);
        ForbiddenKind k = ForbiddenAnalyzer.classifyBlack(b, 7, 7);
        return new Example("双四", "一子同时形成两个四（再下一手可成五）。", b, new Pos(7, 7), k);
    }
}
