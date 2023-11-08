package com.lhf.game.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

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

    public static Set<CreatureFaction> competeSet(CreatureFaction aFaction) {
        if (aFaction == null || RENEGADE.equals(aFaction)) {
            return Collections.unmodifiableSet(EnumSet.allOf(CreatureFaction.class));
        }
        switch (aFaction) {
            case MONSTER:
                return Collections.unmodifiableSet(EnumSet.of(PLAYER, RENEGADE));
            case NPC:
                return Collections.unmodifiableSet(EnumSet.of(MONSTER, RENEGADE));
            case PLAYER:
                return Collections.unmodifiableSet(EnumSet.of(MONSTER, RENEGADE));
            case RENEGADE:
                return Collections.unmodifiableSet(EnumSet.allOf(CreatureFaction.class));
            default:
                return Collections.unmodifiableSet(EnumSet.allOf(CreatureFaction.class));
        }
    }

    public Set<CreatureFaction> competeSet() {
        return CreatureFaction.competeSet(this);
    }

    public static Set<CreatureFaction> allySet(CreatureFaction aFaction) {
        if (aFaction == null || RENEGADE.equals(aFaction)) {
            return Collections.unmodifiableSet(EnumSet.noneOf(CreatureFaction.class));
        }

        switch (aFaction) {
            case MONSTER:
                return Collections.unmodifiableSet(EnumSet.of(MONSTER));
            case NPC:
                return Collections.unmodifiableSet(EnumSet.of(PLAYER, NPC));
            case PLAYER:
                return Collections.unmodifiableSet(EnumSet.of(PLAYER, NPC));
            case RENEGADE:
                return Collections.unmodifiableSet(EnumSet.noneOf(CreatureFaction.class));
            default:
                return Collections.unmodifiableSet(EnumSet.noneOf(CreatureFaction.class));

        }
    }

    public Set<CreatureFaction> allySet() {
        return CreatureFaction.allySet(this);
    }

    public boolean competing(CreatureFaction other) {
        if (other == null || RENEGADE.equals(other)) {
            return true;
        }
        return CreatureFaction.competeSet(this).contains(other);
    }

    public boolean allied(CreatureFaction other) {
        if (other == null || RENEGADE.equals(other)) {
            return false;
        }
        return CreatureFaction.allySet(this).contains(other);
    }

}
