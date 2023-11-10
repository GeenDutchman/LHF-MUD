package com.lhf.game.enums;

import java.util.Optional;

public enum SpellLevel {
    CANTRIP, FIRST_MAGNITUDE, SECOND_MAGNITUDE, THIRD_MAGNITUDE, FOURTH_MAGNITUDE, FIVTH_MAGNITUDE, SIXTH_MAGNITUDE,
    SEVENTH_MAGNITUDE, EIGHTH_MAGNITUDE, NINTH_MAGNITUDE, TENTH_MAGNITUDE;

    public static Optional<SpellLevel> getSpellLevel(String value) {
        for (SpellLevel vname : values()) {
            if (vname.toString().equals(value) || vname.toString().replace("_", " ").equals(value)
                    || Integer.toString(vname.toInt()).equals(value)) {
                return Optional.of(vname);
            }
        }
        return Optional.empty();
    }

    public static boolean isSpellLevel(String value) {
        return SpellLevel.getSpellLevel(value).isPresent();
    }

    public static SpellLevel fromInt(int level) {
        if (level < 0) {
            return CANTRIP;
        } else if (level > 10) {
            return TENTH_MAGNITUDE;
        }
        for (SpellLevel spellLevel : values()) {
            if (spellLevel.ordinal() == level) {
                return spellLevel;
            }
        }
        return CANTRIP;
    }

    public int toInt() {
        return this.ordinal();
    }

    @Override
    public String toString() {
        return this.name().replace("_", " ");
    }

}
