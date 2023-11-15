package com.lhf.game.enums;

import java.util.Optional;

public enum ResourceCost {
    NO_COST, FIRST_MAGNITUDE, SECOND_MAGNITUDE, THIRD_MAGNITUDE, FOURTH_MAGNITUDE, FIVTH_MAGNITUDE, SIXTH_MAGNITUDE,
    SEVENTH_MAGNITUDE, EIGHTH_MAGNITUDE, NINTH_MAGNITUDE, TENTH_MAGNITUDE;

    public static Optional<ResourceCost> getResourceCostFromString(String value) {
        for (ResourceCost vname : values()) {
            if (vname.toString().equals(value) || vname.toString().replace("_", " ").equals(value)
                    || Integer.toString(vname.toInt()).equals(value)) {
                return Optional.of(vname);
            }
        }
        return Optional.empty();
    }

    public static boolean isResourceCost(String value) {
        return ResourceCost.getResourceCostFromString(value).isPresent();
    }

    public static ResourceCost fromInt(int level) {
        if (level <= 0) {
            return NO_COST;
        } else if (level >= 10) {
            return TENTH_MAGNITUDE;
        }
        for (ResourceCost cost : values()) {
            if (cost.ordinal() == level) {
                return cost;
            }
        }
        return NO_COST;
    }

    public int toInt() {
        return this.ordinal();
    }

    @Override
    public String toString() {
        return this.name().replace("_", " ");
    }

}
