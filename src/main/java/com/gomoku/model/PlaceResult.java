package com.gomoku.model;

public final class PlaceResult {
    public final boolean accepted;
    public final String message;
    public final ForbiddenKind forbidden;
    public final GameStatus status;

    public PlaceResult(boolean accepted, String message, ForbiddenKind forbidden, GameStatus status) {
        this.accepted = accepted;
        this.message = message;
        this.forbidden = forbidden;
        this.status = status;
    }

    public static PlaceResult ok(GameStatus status) {
        return new PlaceResult(true, "落子成功", ForbiddenKind.NONE, status);
    }

    public static PlaceResult reject(String message, ForbiddenKind forbidden, GameStatus status) {
        return new PlaceResult(false, message, forbidden, status);
    }
}
