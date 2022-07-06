package com.lhf.game.magic;

import com.lhf.Taggable;
import com.lhf.game.dice.Dice.RollResult;

public interface CubeHolder extends Taggable {
    public String getName();

    public String getCasterVocation();

    public Integer getCasterDifficulty();

    public RollResult spellAttack();

}
