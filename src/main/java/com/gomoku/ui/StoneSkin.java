package com.gomoku.ui;

public enum StoneSkin {
    JADE("jade", "墨玉"),
    CANDY("candy", "糖果"),
    GLASS("glass", "玻璃"),
    CLASSIC("classic", "经典玉石"),
    NEON("neon", "霓虹"),
    WOOD("wood", "木纹"),
    FLAT("flat", "扁平");

    public final String id;
    public final String label;

    StoneSkin(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public static StoneSkin byId(String id) {
        for (StoneSkin s : values()) {
            if (s.id.equals(id)) {
                return s;
            }
        }
        return JADE;
    }
}
