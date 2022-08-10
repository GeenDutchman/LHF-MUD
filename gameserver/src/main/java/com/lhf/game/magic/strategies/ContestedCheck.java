package com.lhf.game.magic.strategies;

import com.lhf.game.creature.Creature;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;

public class ContestedCheck extends CasterVsCreatureStrategy {
    private Attributes attrToUse;

    public ContestedCheck(Attributes attrToUse) {
        this.attrToUse = attrToUse;
    }

    public Attributes getAttrToUse() {
        return attrToUse;
    }

    @Override
    public MultiRollResult getTargetEffort(Creature target) {
        return target.check(this.getAttrToUse());
    }

    @Override
    public void setCasterEffort(Creature caster) {
        this.setCasterEffort(caster.check(this.getAttrToUse()));
    }

}
