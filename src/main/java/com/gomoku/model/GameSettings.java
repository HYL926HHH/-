package com.gomoku.model;

public final class GameSettings {
    public int boardSize = 15;
    public GameMode mode = GameMode.LOCAL_PVP;
    public boolean renjuForbidden = true;
    public boolean showForbiddenHints = true;
    public boolean confirmMove = false;
    public int undoLimitPerPlayer = 3;
    public int secondsPerPlayer = 300;
    public boolean countdownEnabled = true;
    public int handicap = 0;
    public boolean swapThree = false;
    public double komi = 0;
    public int blindLastMoves = 0;
    public AiLevel aiLevel = AiLevel.NORMAL;
    public Stone humanStone = Stone.BLACK;
    public String blackName = "黑方";
    public String whiteName = "白方";
    public String themeId = "gufeng";
    public String skinId = "jade";
    public boolean sound = true;
    public boolean animation = true;
    public boolean effects = true;

    public GameSettings copy() {
        GameSettings s = new GameSettings();
        s.boardSize = boardSize;
        s.mode = mode;
        s.renjuForbidden = renjuForbidden;
        s.showForbiddenHints = showForbiddenHints;
        s.confirmMove = confirmMove;
        s.undoLimitPerPlayer = undoLimitPerPlayer;
        s.secondsPerPlayer = secondsPerPlayer;
        s.countdownEnabled = countdownEnabled;
        s.handicap = handicap;
        s.swapThree = swapThree;
        s.komi = komi;
        s.blindLastMoves = blindLastMoves;
        s.aiLevel = aiLevel;
        s.humanStone = humanStone;
        s.blackName = blackName;
        s.whiteName = whiteName;
        s.themeId = themeId;
        s.skinId = skinId;
        s.sound = sound;
        s.animation = animation;
        s.effects = effects;
        return s;
    }
}
