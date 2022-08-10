package com.lhf.game.magic;

import com.lhf.Taggable;
import com.lhf.game.dice.MultiRollResult;

public interface CubeHolder extends Taggable {
    public String getName();

    public String getCasterVocation();

    public Integer getCasterDifficulty();

    public MultiRollResult spellAttack();

}
