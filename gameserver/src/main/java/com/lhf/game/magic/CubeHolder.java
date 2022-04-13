package com.lhf.game.magic;

import com.lhf.Taggable;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.enums.Attributes;

public interface CubeHolder extends Taggable {
    public String getName();

    public String getCasterVocation();

    public Integer getCasterLevels();

    public Integer getCasterDifficulty();

    public RollResult spellAttack();

    public RollResult check(Attributes attribute);

}
