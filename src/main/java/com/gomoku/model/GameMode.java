package com.gomoku.model;

public enum GameMode {
    LOCAL_PVP("双人对战"),
    PVE("人机对战"),
    NETWORK("联网房间"),
    PUZZLE("死活 / 残局"),
    REVIEW("打谱练习");

    public final String label;

    GameMode(String label) {
        this.label = label;
    }
}
