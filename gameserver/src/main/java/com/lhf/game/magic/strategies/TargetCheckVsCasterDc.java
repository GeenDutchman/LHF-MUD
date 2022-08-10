package com.lhf.game.magic.strategies;

import com.lhf.game.creature.Creature;
import com.lhf.game.dice.DiceDC;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.magic.CubeHolder;

public class TargetCheckVsCasterDc extends CasterVsCreatureStrategy {
    private Attributes attrToUse;

    public TargetCheckVsCasterDc(Attributes attrToUse) {
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
        CubeHolder ch = (CubeHolder) caster.getVocation().get();
        this.setCasterEffort(new MultiRollResult(new DiceDC(ch.getCasterDifficulty()).rollDice()));
    }

}
