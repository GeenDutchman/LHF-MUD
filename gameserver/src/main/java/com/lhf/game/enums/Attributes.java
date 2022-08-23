package com.lhf.game.enums;

public enum Attributes {
    STR, DEX, CON, INT, WIS, CHA;

    public static Attributes getAttribute(String value) {
        for (Attributes attr : values()) {
            if (attr.toString().equalsIgnoreCase(value)) {
                return attr;
            }
        }
        return null;
    }

    public static boolean isCreatureFaction(String value) {
        return Attributes.getAttribute(value) != null;
    }
}
