package com.lhf.game;

public enum TickType {
    INSTANT, ACTION, TURN, ROUND, BATTLE, ROOM, CONDITIONAL;

    public static TickType getTickType(String value) {
        for (TickType type : values()) {
            if (type.toString().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }

    public static boolean isTickType(String value) {
        return TickType.getTickType(value) != null;
    }
}