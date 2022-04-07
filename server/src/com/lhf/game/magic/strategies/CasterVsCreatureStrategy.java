package com.lhf.game.magic.strategies;

import java.util.Map;

import com.lhf.game.creature.Creature;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.magic.CubeHolder;

public abstract class CasterVsCreatureStrategy {
    protected RollResult casterEffort;

    public CasterVsCreatureStrategy() {
    }

    public RollResult getCasterEffort() {
        return this.casterEffort;
    }

    protected void setCasterEffort(RollResult casterEffort) {
        this.casterEffort = casterEffort;
    }

    public abstract void setCasterEffort(CubeHolder caster);

    public abstract RollResult getTargetEffort(Creature target);
}
