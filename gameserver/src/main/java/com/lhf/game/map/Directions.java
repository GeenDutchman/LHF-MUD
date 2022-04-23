package com.lhf.game.map;

public enum Directions {
    NORTH, SOUTH, EAST, WEST, UP, DOWN;

    public static Boolean isDirections(String value) {
        for (Directions dir : values()) {
            if (dir.toString().equalsIgnoreCase(value)
                    || (value.length() == 1 && dir.toString().substring(0, 1).equalsIgnoreCase(value))) {
                return true;
            }
        }
        return false;
    }

    public static Directions getDirections(String value) {
        for (Directions dir : values()) {
            if (dir.toString().equalsIgnoreCase(value)
                    || (value.length() == 1 && dir.toString().substring(0, 1).equalsIgnoreCase(value))) {
                return dir;
            }
        }
        return null;
    }
}
