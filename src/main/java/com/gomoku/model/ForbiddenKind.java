package com.gomoku.model;

public enum ForbiddenKind {
    NONE("合法"),
    OVERLINE("长连"),
    DOUBLE_THREE("双三"),
    DOUBLE_FOUR("双四");

    public final String label;

    ForbiddenKind(String label) {
        this.label = label;
    }
}
