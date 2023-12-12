package com.lhf.game.magic;

import java.util.EnumSet;

import com.lhf.Taggable;
import com.lhf.game.EntityEffect;
import com.lhf.game.enums.ResourceCost;

public interface CubeHolder extends Taggable {
    public String getName();

    public String getCasterVocation();

    public default int getCastingBonus(final EntityEffect effect) {
        return 0;
    }

    public String printMagnitudes();

    boolean useMagnitude(ResourceCost level); // package private

    public default boolean canUseMagnitude(ResourceCost level) {
        return this.availableMagnitudes().contains(level);
    }

    public EnumSet<ResourceCost> availableMagnitudes();
}
