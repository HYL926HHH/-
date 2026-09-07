package com.gomoku.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Game {
    private final GameSettings settings;
    private Board board;
    private final List<Move> moves = new ArrayList<>();
    private Stone toMove = Stone.BLACK;
    private GameStatus status = GameStatus.PLAYING;
    private int blackUndosLeft;
    private int whiteUndosLeft;
    private int blackSeconds;
    private int whiteSeconds;
    private boolean drawOfferedByBlack;
    private boolean drawOfferedByWhite;
    private boolean swapResolved;
    private Pos pending;
    private Stone winner;
    private String lastMessage = "黑棋先行";
    private final java.util.ArrayList<Pos> winningLine = new java.util.ArrayList<>();

    public Game(GameSettings settings) {
        this.settings = settings.copy();
        this.board = new Board(this.settings.boardSize);
        this.blackUndosLeft = settings.undoLimitPerPlayer;
        this.whiteUndosLeft = settings.undoLimitPerPlayer;
        this.blackSeconds = settings.secondsPerPlayer;
        this.whiteSeconds = settings.secondsPerPlayer;
        applyHandicap();
        if (this.settings.handicap == 0 && this.settings.swapThree) {
            lastMessage = "三手交换：前三手后由白方选择是否换子";
        }
    }

    private void applyHandicap() {
        int n = settings.boardSize;
        int star = n >= 15 ? 3 : 2;
        int far = n - 1 - star;
        int mid = n / 2;
        if (settings.handicap >= 2) {
            board.set(star, star, Stone.BLACK);
            board.set(far, far, Stone.BLACK);
            moves.add(new Move(star, star, Stone.BLACK, System.currentTimeMillis()));
            moves.add(new Move(far, far, Stone.BLACK, System.currentTimeMillis()));
            toMove = Stone.WHITE;
            lastMessage = "让二子：白棋先行";
        } else if (settings.handicap == 1) {
            toMove = Stone.WHITE;
            lastMessage = "让先：白棋先行";
        }
        if (settings.handicap >= 3) {
            board.set(star, far, Stone.BLACK);
            moves.add(new Move(star, far, Stone.BLACK, System.currentTimeMillis()));
        }
        if (settings.mode == GameMode.REVIEW) {
            lastMessage = "打谱模式：可前进后退";
        }
        if (mid > 0 && settings.handicap == 0) {
            // keep empty
        }
    }

    public GameSettings settings() {
        return settings;
    }

    public Board board() {
        return board;
    }

    public List<Move> moves() {
        return moves;
    }

    public Stone toMove() {
        return toMove;
    }

    public GameStatus status() {
        return status;
    }

    public String lastMessage() {
        return lastMessage;
    }

    public Pos pending() {
        return pending;
    }

    public int secondsLeft(Stone stone) {
        return stone == Stone.BLACK ? blackSeconds : whiteSeconds;
    }

    public int undosLeft(Stone stone) {
        return stone == Stone.BLACK ? blackUndosLeft : whiteUndosLeft;
    }

    public java.util.List<Pos> winningLine() {
        return winningLine;
    }

    public Optional<Stone> winner() {
        return Optional.ofNullable(winner);
    }

    public boolean isOver() {
        return status == GameStatus.BLACK_WIN
                || status == GameStatus.WHITE_WIN
                || status == GameStatus.DRAW;
    }

    public PlaceResult click(int x, int y) {
        if (isOver()) {
            return PlaceResult.reject("对局已结束", ForbiddenKind.NONE, status);
        }
        if (status == GameStatus.WAITING_SWAP) {
            return PlaceResult.reject("请先完成三手交换", ForbiddenKind.NONE, status);
        }
        if (!board.isEmpty(x, y)) {
            return PlaceResult.reject("该交叉点已有棋子", ForbiddenKind.NONE, status);
        }
        if (settings.confirmMove) {
            if (pending != null && pending.x == x && pending.y == y) {
                pending = null;
                return commit(x, y);
            }
            pending = new Pos(x, y);
            lastMessage = "待确认落子：" + pending + "（再点一次或按确认）";
            status = GameStatus.WAITING_CONFIRM;
            return new PlaceResult(false, lastMessage, ForbiddenKind.NONE, status);
        }
        return commit(x, y);
    }

    public PlaceResult confirmPending() {
        if (pending == null) {
            return PlaceResult.reject("没有待确认的落子", ForbiddenKind.NONE, status);
        }
        Pos p = pending;
        pending = null;
        return commit(p.x, p.y);
    }

    public void cancelPending() {
        pending = null;
        if (status == GameStatus.WAITING_CONFIRM) {
            status = GameStatus.PLAYING;
        }
        lastMessage = "已取消确认";
    }

    public PlaceResult commit(int x, int y) {
        if (isOver() || !board.isEmpty(x, y)) {
            return PlaceResult.reject("无法落子", ForbiddenKind.NONE, status);
        }
        Stone stone = toMove;
        if (stone == Stone.BLACK && settings.renjuForbidden) {
            ForbiddenKind kind = ForbiddenAnalyzer.previewForbidden(board, x, y, true);
            if (kind != ForbiddenKind.NONE) {
                lastMessage = "禁手：" + kind.label + "（" + Pos.toAlgebraic(x, y) + "）";
                return PlaceResult.reject(lastMessage, kind, status);
            }
        }
        board.set(x, y, stone);
        Move move = new Move(x, y, stone, System.currentTimeMillis());
        if (stone == Stone.BLACK && settings.showForbiddenHints) {
            move.forbiddenHint = false;
        }
        moves.add(move);
        if ((stone == Stone.BLACK && ForbiddenAnalyzer.isExactFive(board, x, y, stone))
                || (stone == Stone.WHITE && ForbiddenAnalyzer.isFiveOrMore(board, x, y, stone))) {
            winner = stone;
            status = stone == Stone.BLACK ? GameStatus.BLACK_WIN : GameStatus.WHITE_WIN;
            lastMessage = stone.displayName() + "棋连五获胜";
            captureWinningLine(x, y, stone);
            return new PlaceResult(true, lastMessage, ForbiddenKind.NONE, status);
        }
        if (board.isFull()) {
            return settleDrawOrKomi();
        }
        toMove = stone.opponent();
        status = GameStatus.PLAYING;
        if (settings.swapThree && !swapResolved && settings.handicap == 0 && moves.size() == 3) {
            status = GameStatus.WAITING_SWAP;
            lastMessage = "三手已落，白方可选择换子或继续";
            return new PlaceResult(true, lastMessage, ForbiddenKind.NONE, status);
        }
        lastMessage = toMove.displayName() + "棋走子";
        return new PlaceResult(true, lastMessage, ForbiddenKind.NONE, status);
    }

    private void captureWinningLine(int x, int y, Stone stone) {
        winningLine.clear();
        for (int[] d : ForbiddenAnalyzer.DIRS) {
            int len = ForbiddenAnalyzer.lineLength(board, x, y, stone, d[0], d[1]);
            boolean ok = stone == Stone.WHITE ? len >= 5 : len == 5;
            if (!ok) {
                continue;
            }
            winningLine.add(new Pos(x, y));
            for (int s = 1; s < board.size(); s++) {
                int nx = x + d[0] * s;
                int ny = y + d[1] * s;
                if (!board.inBounds(nx, ny) || board.get(nx, ny) != stone) {
                    break;
                }
                winningLine.add(new Pos(nx, ny));
            }
            for (int s = 1; s < board.size(); s++) {
                int nx = x - d[0] * s;
                int ny = y - d[1] * s;
                if (!board.inBounds(nx, ny) || board.get(nx, ny) != stone) {
                    break;
                }
                winningLine.add(new Pos(nx, ny));
            }
            break;
        }
    }

    private PlaceResult settleDrawOrKomi() {
        if (settings.komi > 0) {
            winner = Stone.WHITE;
            status = GameStatus.WHITE_WIN;
            lastMessage = "满盘且贴目 " + settings.komi + "，判白胜";
            return new PlaceResult(true, lastMessage, ForbiddenKind.NONE, status);
        } else {
            status = GameStatus.DRAW;
            lastMessage = "满盘和棋";
            return new PlaceResult(true, lastMessage, ForbiddenKind.NONE, status);
        }
    }

    public boolean swapColors(boolean doSwap) {
        if (status != GameStatus.WAITING_SWAP) {
            return false;
        }
        swapResolved = true;
        if (doSwap) {
            String bn = settings.blackName;
            settings.blackName = settings.whiteName;
            settings.whiteName = bn;
            Stone hs = settings.humanStone;
            settings.humanStone = hs.opponent();
            lastMessage = "已换子：执黑执白对调，仍由白方走第 4 手";
        } else {
            lastMessage = "不换子，白方继续";
        }
        status = GameStatus.PLAYING;
        toMove = Stone.WHITE;
        return true;
    }

    public boolean undo(Stone who) {
        if (moves.isEmpty() || isOver()) {
            return false;
        }
        int left = who == Stone.BLACK ? blackUndosLeft : whiteUndosLeft;
        if (left <= 0 && settings.mode != GameMode.REVIEW) {
            lastMessage = "悔棋次数已用完";
            return false;
        }
        int steps = settings.mode == GameMode.PVE ? 2 : 1;
        steps = Math.min(steps, moves.size());
        for (int i = 0; i < steps; i++) {
            Move m = moves.remove(moves.size() - 1);
            board.set(m.x, m.y, null);
        }
        if (settings.mode != GameMode.REVIEW) {
            if (who == Stone.BLACK) {
                blackUndosLeft--;
            } else {
                whiteUndosLeft--;
            }
        }
        toMove = moves.isEmpty() ? (settings.handicap >= 1 ? Stone.WHITE : Stone.BLACK)
                : moves.get(moves.size() - 1).stone.opponent();
        if (settings.handicap >= 2 && moves.size() <= 2) {
            toMove = Stone.WHITE;
        }
        pending = null;
        status = GameStatus.PLAYING;
        winner = null;
        winningLine.clear();
        lastMessage = "悔棋成功，轮到" + toMove.displayName() + "棋";
        return true;
    }

    public void resign(Stone who) {
        winner = who.opponent();
        status = winner == Stone.BLACK ? GameStatus.BLACK_WIN : GameStatus.WHITE_WIN;
        lastMessage = who.displayName() + "棋认输";
    }

    public String offerDraw(Stone who) {
        if (who == Stone.BLACK) {
            drawOfferedByBlack = true;
        } else {
            drawOfferedByWhite = true;
        }
        if (drawOfferedByBlack && drawOfferedByWhite) {
            status = GameStatus.DRAW;
            lastMessage = "双方同意和棋";
            return lastMessage;
        }
        lastMessage = who.displayName() + "棋提出和棋，等待对方同意";
        return lastMessage;
    }

    public boolean tickSecond() {
        if (!settings.countdownEnabled || isOver() || status == GameStatus.WAITING_SWAP) {
            return false;
        }
        if (toMove == Stone.BLACK) {
            blackSeconds--;
            if (blackSeconds <= 0) {
                winner = Stone.WHITE;
                status = GameStatus.WHITE_WIN;
                lastMessage = "黑棋超时负";
                return true;
            }
        } else {
            whiteSeconds--;
            if (whiteSeconds <= 0) {
                winner = Stone.BLACK;
                status = GameStatus.BLACK_WIN;
                lastMessage = "白棋超时负";
                return true;
            }
        }
        return false;
    }

    public void loadMoves(List<Move> record, int playUntil) {
        board.clear();
        moves.clear();
        toMove = settings.handicap >= 1 ? Stone.WHITE : Stone.BLACK;
        status = GameStatus.PLAYING;
        winner = null;
        winningLine.clear();
        int n = Math.min(playUntil, record.size());
        for (int i = 0; i < n; i++) {
            Move m = record.get(i);
            board.set(m.x, m.y, m.stone);
            moves.add(m);
            toMove = m.stone.opponent();
        }
        lastMessage = "打谱到第 " + n + " 手";
    }

    public StyleReport analyzeStyle() {
        int attack = 0;
        int defense = 0;
        Board sim = new Board(board.size());
        for (Move m : moves) {
            int beforeOpp = threatScore(sim, m.stone.opponent());
            sim.set(m.x, m.y, m.stone);
            int afterSelf = threatScore(sim, m.stone);
            int afterOpp = threatScore(sim, m.stone.opponent());
            if (afterSelf > 2000) {
                attack++;
            }
            if (afterOpp < beforeOpp) {
                defense++;
            }
        }
        String style;
        if (attack > defense + 2) {
            style = "攻击型棋风";
        } else if (defense > attack + 2) {
            style = "稳健防守型";
        } else {
            style = "均衡型";
        }
        return new StyleReport(style, attack, defense);
    }

    private int threatScore(Board b, Stone stone) {
        int s = 0;
        for (int y = 0; y < b.size(); y++) {
            for (int x = 0; x < b.size(); x++) {
                if (b.get(x, y) != stone) {
                    continue;
                }
                for (int[] d : ForbiddenAnalyzer.DIRS) {
                    int len = ForbiddenAnalyzer.lineLength(b, x, y, stone, d[0], d[1]);
                    if (len >= 4) {
                        s += 5000;
                    } else if (len == 3) {
                        s += 800;
                    }
                }
            }
        }
        return s;
    }

    public record StyleReport(String label, int attackMoves, int defenseMoves) {
        @Override
        public String toString() {
            return label + "（进攻手 " + attackMoves + " / 防守手 " + defenseMoves + "）";
        }
    }
}
