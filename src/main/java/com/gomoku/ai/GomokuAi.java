package com.gomoku.ai;

import com.gomoku.model.AiLevel;
import com.gomoku.model.Board;
import com.gomoku.model.ForbiddenAnalyzer;
import com.gomoku.model.Game;
import com.gomoku.model.Pos;
import com.gomoku.model.Stone;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * 简单 AI：优先成五、堵活四，再冲活三；高难度加开局库与浅层搜索。
 */
public final class GomokuAi {
    private final Random random = new Random();
    private final OpeningBook openingBook = new OpeningBook();

    public Pos choose(Game game) {
        Board board = game.board();
        Stone me = game.toMove();
        AiLevel level = game.settings().aiLevel;
        if (level.strength >= 3) {
            Pos book = openingBook.suggest(game);
            if (book != null) {
                return book;
            }
        }
        List<Pos> cands = candidates(board);
        if (cands.isEmpty()) {
            int m = board.size() / 2;
            return new Pos(m, m);
        }
        if (level == AiLevel.EASY) {
            return cands.get(random.nextInt(Math.min(6, cands.size())));
        }
        Pos win = findWinningMove(board, me, game.settings().renjuForbidden);
        if (win != null) {
            return win;
        }
        Pos block = findWinningMove(board, me.opponent(), false);
        if (block != null && legal(board, block.x, block.y, me, game.settings().renjuForbidden)) {
            return block;
        }
        if (level.strength >= 2) {
            return search(board, me, cands, game.settings().renjuForbidden, level.strength >= 3 ? 2 : 1);
        }
        Pos attack = findLiveThreeOrFour(board, me, game.settings().renjuForbidden);
        if (attack != null) {
            return attack;
        }
        Pos deny = findLiveThreeOrFour(board, me.opponent(), false);
        if (deny != null && legal(board, deny.x, deny.y, me, game.settings().renjuForbidden)) {
            return deny;
        }
        return bestHeuristic(board, me, cands, game.settings().renjuForbidden);
    }

    private Pos search(Board board, Stone me, List<Pos> cands, boolean renju, int depth) {
        int best = Integer.MIN_VALUE;
        Pos bestPos = cands.get(0);
        for (Pos p : cands) {
            if (!legal(board, p.x, p.y, me, renju)) {
                continue;
            }
            board.set(p.x, p.y, me);
            int v;
            if (ForbiddenAnalyzer.isFiveOrMore(board, p.x, p.y, me)
                    && (me == Stone.WHITE || ForbiddenAnalyzer.isExactFive(board, p.x, p.y, me))) {
                v = 1_000_000;
            } else {
                v = -negamax(board, me.opponent(), me, depth - 1, -1_000_000, 1_000_000, renju);
            }
            board.set(p.x, p.y, null);
            v += random.nextInt(7);
            if (v > best) {
                best = v;
                bestPos = p;
            }
        }
        return bestPos;
    }

    private int negamax(Board board, Stone toMove, Stone root, int depth, int alpha, int beta, boolean renju) {
        if (depth <= 0) {
            return Evaluator.score(board, root) - Evaluator.score(board, root.opponent());
        }
        int best = Integer.MIN_VALUE / 2;
        for (Pos p : candidates(board)) {
            if (!legal(board, p.x, p.y, toMove, renju)) {
                continue;
            }
            board.set(p.x, p.y, toMove);
            int v;
            boolean win = toMove == Stone.WHITE
                    ? ForbiddenAnalyzer.isFiveOrMore(board, p.x, p.y, toMove)
                    : ForbiddenAnalyzer.isExactFive(board, p.x, p.y, toMove);
            if (win) {
                v = 200_000 + depth * 100;
            } else {
                v = -negamax(board, toMove.opponent(), root, depth - 1, -beta, -alpha, renju);
            }
            board.set(p.x, p.y, null);
            best = Math.max(best, v);
            alpha = Math.max(alpha, v);
            if (alpha >= beta) {
                break;
            }
        }
        return best;
    }

    private Pos findWinningMove(Board board, Stone stone, boolean renju) {
        for (Pos p : candidates(board)) {
            if (!legal(board, p.x, p.y, stone, renju && stone == Stone.BLACK)) {
                continue;
            }
            board.set(p.x, p.y, stone);
            boolean win = stone == Stone.WHITE
                    ? ForbiddenAnalyzer.isFiveOrMore(board, p.x, p.y, stone)
                    : ForbiddenAnalyzer.isExactFive(board, p.x, p.y, stone);
            board.set(p.x, p.y, null);
            if (win) {
                return p;
            }
        }
        return null;
    }

    private Pos findLiveThreeOrFour(Board board, Stone stone, boolean renju) {
        int best = 0;
        Pos pos = null;
        for (Pos p : candidates(board)) {
            if (!legal(board, p.x, p.y, stone, renju && stone == Stone.BLACK)) {
                continue;
            }
            board.set(p.x, p.y, stone);
            int s = Evaluator.patternGain(board, p.x, p.y, stone);
            board.set(p.x, p.y, null);
            if (s > best) {
                best = s;
                pos = p;
            }
        }
        return best >= Evaluator.LIVE_THREE ? pos : null;
    }

    private Pos bestHeuristic(Board board, Stone me, List<Pos> cands, boolean renju) {
        return cands.stream()
                .filter(p -> legal(board, p.x, p.y, me, renju))
                .max(Comparator.comparingInt(p -> {
                    board.set(p.x, p.y, me);
                    int s = Evaluator.patternGain(board, p.x, p.y, me)
                            + Evaluator.patternGain(board, p.x, p.y, me.opponent()) / 2
                            + centerBias(board, p);
                    board.set(p.x, p.y, null);
                    return s + random.nextInt(5);
                }))
                .orElse(cands.get(0));
    }

    private int centerBias(Board board, Pos p) {
        int m = board.size() / 2;
        int d = Math.abs(p.x - m) + Math.abs(p.y - m);
        return Math.max(0, 40 - d * 3);
    }

    private boolean legal(Board board, int x, int y, Stone stone, boolean renju) {
        if (!board.isEmpty(x, y)) {
            return false;
        }
        if (stone == Stone.BLACK && renju) {
            return !ForbiddenAnalyzer.isForbiddenMove(board, x, y, true);
        }
        return true;
    }

    static List<Pos> candidates(Board board) {
        boolean any = false;
        List<Pos> list = new ArrayList<>();
        int n = board.size();
        boolean[][] add = new boolean[n][n];
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                if (board.get(x, y) == null) {
                    continue;
                }
                any = true;
                for (int dy = -2; dy <= 2; dy++) {
                    for (int dx = -2; dx <= 2; dx++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (board.isEmpty(nx, ny) && !add[ny][nx]) {
                            add[ny][nx] = true;
                            list.add(new Pos(nx, ny));
                        }
                    }
                }
            }
        }
        if (!any) {
            list.add(new Pos(n / 2, n / 2));
        }
        return list;
    }
}
