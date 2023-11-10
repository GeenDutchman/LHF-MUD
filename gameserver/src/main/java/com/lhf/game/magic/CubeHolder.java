package com.lhf.game.magic;

import java.util.EnumSet;

import com.lhf.Taggable;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.SpellLevel;

public interface CubeHolder extends Taggable {
    public String getName();

    public String getCasterVocation();

    public Integer getCasterDifficulty();

    public MultiRollResult spellAttack();

    public String printMagnitudes();

    boolean useMagnitude(SpellLevel level); // package private

    public default boolean canUseMagnitude(SpellLevel level) {
        return this.availableMagnitudes().contains(level);
    }

    public EnumSet<SpellLevel> availableMagnitudes();
}
