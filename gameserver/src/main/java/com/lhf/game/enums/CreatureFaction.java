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

    public boolean competing(CreatureFaction other) {
        if (other == null || RENEGADE.equals(other)) {
            return true;
        }
        switch (this) {
            case MONSTER:
                return PLAYER.equals(other);
            case NPC:
                return false;
            case PLAYER:
                return MONSTER.equals(other);
            case RENEGADE:
                return true;
            default:
                return true;
        }
    }

    public static boolean areCompeting(CreatureFaction one, CreatureFaction two) {
        if (one != null) {
            return one.competing(two);
        } else if (two != null) {
            return two.competing(one);
        }
        return true;
    }
}
