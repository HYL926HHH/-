package com.gomoku.persist;

import com.gomoku.model.Move;
import com.gomoku.model.Stone;

import java.util.ArrayList;
import java.util.List;

public final class GameRecord {
    public String id = Long.toString(System.currentTimeMillis(), 36);
    public int boardSize = 15;
    public String blackName = "黑方";
    public String whiteName = "白方";
    public String result = "*";
    public String mode = "LOCAL_PVP";
    public boolean forbidden;
    public int handicap;
    public double komi;
    public final List<Move> moves = new ArrayList<>();
    public final List<String> annotations = new ArrayList<>();

    public String toText() {
        StringBuilder sb = new StringBuilder();
        sb.append("#gomoku 1\n");
        sb.append("id ").append(id).append('\n');
        sb.append("size ").append(boardSize).append('\n');
        sb.append("black ").append(esc(blackName)).append('\n');
        sb.append("white ").append(esc(whiteName)).append('\n');
        sb.append("result ").append(result).append('\n');
        sb.append("mode ").append(mode).append('\n');
        sb.append("forbidden ").append(forbidden).append('\n');
        sb.append("handicap ").append(handicap).append('\n');
        sb.append("komi ").append(komi).append('\n');
        for (int i = 0; i < moves.size(); i++) {
            Move m = moves.get(i);
            sb.append("move ").append(m.stone == Stone.BLACK ? 'B' : 'W').append(' ')
                    .append(m.x).append(' ').append(m.y);
            if (m.annotation != null && !m.annotation.isBlank()) {
                sb.append(" # ").append(esc(m.annotation));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public static GameRecord parse(String text) {
        GameRecord r = new GameRecord();
        r.moves.clear();
        for (String raw : text.split("\n")) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] p = line.split("\\s+", 3);
            switch (p[0]) {
                case "id" -> r.id = p.length > 1 ? p[1] : r.id;
                case "size" -> r.boardSize = Integer.parseInt(p[1]);
                case "black" -> r.blackName = unesc(p.length > 1 ? line.substring(6).trim() : "黑方");
                case "white" -> r.whiteName = unesc(p.length > 1 ? line.substring(6).trim() : "白方");
                case "result" -> r.result = p[1];
                case "mode" -> r.mode = p[1];
                case "forbidden" -> r.forbidden = Boolean.parseBoolean(p[1]);
                case "handicap" -> r.handicap = Integer.parseInt(p[1]);
                case "komi" -> r.komi = Double.parseDouble(p[1]);
                case "move" -> {
                    String[] mp = line.split("\\s+");
                    Stone s = mp[1].startsWith("B") ? Stone.BLACK : Stone.WHITE;
                    Move m = new Move(Integer.parseInt(mp[2]), Integer.parseInt(mp[3]), s, 0);
                    int hash = line.indexOf('#');
                    if (hash >= 0) {
                        m.annotation = unesc(line.substring(hash + 1).trim());
                    }
                    r.moves.add(m);
                }
                default -> {
                }
            }
        }
        return r;
    }

    private static String esc(String s) {
        return s.replace('\n', ' ');
    }

    private static String unesc(String s) {
        return s;
    }
}
