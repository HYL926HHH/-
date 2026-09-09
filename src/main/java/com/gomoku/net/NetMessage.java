package com.gomoku.net;

import com.gomoku.model.Stone;

public final class NetMessage {
    public final String type;
    public final String body;

    public NetMessage(String type, String body) {
        this.type = type;
        this.body = body == null ? "" : body;
    }

    public String encode() {
        return type + " " + body.replace('\n', ' ') + "\n";
    }

    public static NetMessage parse(String line) {
        int sp = line.indexOf(' ');
        if (sp < 0) {
            return new NetMessage(line.trim(), "");
        }
        return new NetMessage(line.substring(0, sp).trim(), line.substring(sp + 1).trim());
    }

    public static NetMessage place(int x, int y, Stone stone) {
        return new NetMessage("PLACE", stone.name() + " " + x + " " + y);
    }

    public static NetMessage chat(String from, String text) {
        return new NetMessage("CHAT", from + "|" + text);
    }
}
