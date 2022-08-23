package com.lhf.game.enums;

public enum Stats {
    MAXHP, CURRENTHP, XPEARNED, XPWORTH, PROFICIENCYBONUS, AC;

    public static Stats getStat(String value) {
        for (Stats stat : values()) {
            if (stat.toString().equalsIgnoreCase(value)) {
                return stat;
            }
        }
        return null;
    }

    public static boolean isStat(String value) {
        return Stats.getStat(value) != null;
    }
}
