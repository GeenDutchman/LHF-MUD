package com.lhf.game.magic.strategies;

import com.lhf.game.creature.Creature;
import com.lhf.game.dice.DiceDC;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.enums.Stats;
import com.lhf.game.magic.CubeHolder;

public class CasterCheckVsTargetArmorClass extends CasterVsCreatureStrategy {

    public CasterCheckVsTargetArmorClass() {
    }

    @Override
    public MultiRollResult getTargetEffort(Creature target) {
        return new MultiRollResult(new DiceDC(target.getStats().get(Stats.AC)).rollDice());
    }

    @Override
    public void setCasterEffort(Creature caster) {
        CubeHolder ch = (CubeHolder) caster.getVocation().get();
        this.setCasterEffort(ch.spellAttack());
    }

}
