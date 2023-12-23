package com.lhf.game;

/**
 * Used to denote time passing, and sent at the END of whatever duration it
 * connotates.
 * {@link #INSTANT} is the weird one here, becuase the END of something of
 * duration 0 is the START.
 */
public enum TickType {
    /**
     * Happens in that instant, has 0 duration, needs immediate application and no
     * take-backsies.
     */
    INSTANT,
    /**
     * Small, well, actions, that don't really have an effect on much outside.
     */
    ACTION,
    /**
     * In battle, something that takes up a turn.
     */
    TURN,
    /**
     * In battle, the time it takes for everyone to take a turn.
     */
    ROUND,
    /**
     * The battle is over, and survival is assumed.
     */
    BATTLE,
    /**
     * When a room is left.
     */
    ROOM,
    /**
     * For various other checks.
     */
    CONDITIONAL;

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