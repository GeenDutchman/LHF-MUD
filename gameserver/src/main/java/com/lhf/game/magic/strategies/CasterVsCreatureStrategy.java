package com.lhf.game.magic.strategies;

import com.lhf.game.creature.Creature;
import com.lhf.game.dice.MultiRollResult;

public abstract class CasterVsCreatureStrategy {
    protected MultiRollResult casterEffort;

    public CasterVsCreatureStrategy() {
    }

    public MultiRollResult getCasterEffort() {
        return this.casterEffort;
    }

    protected void setCasterEffort(MultiRollResult casterEffort) {
        this.casterEffort = casterEffort;
    }

    public abstract void setCasterEffort(Creature caster);

    public abstract MultiRollResult getTargetEffort(Creature target);
}
