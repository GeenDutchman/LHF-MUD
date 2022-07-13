package com.lhf.game.magic.strategies;

import com.lhf.game.creature.Creature;
import com.lhf.game.dice.Dice.RollResult;

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

    public abstract void setCasterEffort(Creature caster);

    public abstract RollResult getTargetEffort(Creature target);
}
