package com.lhf.game.enums;

public enum CreatureFaction {
    PLAYER, MONSTER, NPC, RENEGADE;

    public static CreatureFaction getFaction(String value) {
        for (CreatureFaction faction : values()) {
            if (faction.toString().equalsIgnoreCase(value)) {
                return faction;
            }
        }
        return null;
    }

    public static Boolean isCreatureFaction(String value) {
        return CreatureFaction.getFaction(value) != null;
    }
}
