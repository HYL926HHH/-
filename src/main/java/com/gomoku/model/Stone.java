package com.gomoku.model;

public enum Stone {
    BLACK, WHITE;

    public Stone opponent() {
        return this == BLACK ? WHITE : BLACK;
    }

    public String displayName() {
        return this == BLACK ? "黑" : "白";
    }
}
