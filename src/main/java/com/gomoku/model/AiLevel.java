package com.gomoku.model;

public enum AiLevel {
    EASY("入门", 0),
    NORMAL("普通：堵活四 / 冲活三", 1),
    HARD("困难：多层估值", 2),
    MASTER("大师：开局库 + 深搜", 3);

    public final String label;
    public final int strength;

    AiLevel(String label, int strength) {
        this.label = label;
        this.strength = strength;
    }
}
