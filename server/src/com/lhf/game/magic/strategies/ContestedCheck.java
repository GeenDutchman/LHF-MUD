package com.lhf.game.magic.strategies;

import com.lhf.game.creature.Creature;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.magic.CubeHolder;

public class ContestedCheck extends CasterVsCreatureStrategy {
    private Attributes attrToUse;

    public ContestedCheck(Attributes attrToUse) {
        this.attrToUse = attrToUse;
    }

    public Attributes getAttrToUse() {
        return attrToUse;
    }

    @Override
    public RollResult getTargetEffort(Creature target) {
        return target.check(this.getAttrToUse());
    }

    @Override
    public void setCasterEffort(CubeHolder caster) {
        this.setCasterEffort(caster.check(this.getAttrToUse()));
    }

}
