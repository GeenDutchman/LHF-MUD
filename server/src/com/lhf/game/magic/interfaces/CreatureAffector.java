package com.lhf.game.magic.interfaces;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.lhf.game.creature.Creature;
import com.lhf.game.magic.ISpell;
import com.lhf.game.magic.strategies.CasterVsCreatureStrategy;

public abstract class CreatureAffector extends ISpell {
    protected Map<Creature, Creature> targets;
    protected Boolean singleTarget;
    protected Optional<CasterVsCreatureStrategy> strategy;

    protected CreatureAffector(Integer level, String name, String description, Boolean singleTarget) {
        super(level, name, description);
        this.targets = new HashMap<>();
        this.singleTarget = singleTarget;
        this.strategy = Optional.empty();
    }

    public CreatureAffector addTarget(Creature target) {
        if (this.singleTarget && this.targets.size() > 0) {
            this.targets.clear();
        }
        this.targets.put(target, target);
        return this;
    }

    public Set<Creature> getTargets() {
        return this.targets.keySet();
    }

    public Optional<CasterVsCreatureStrategy> getStrategy() {
        return this.strategy;
    }

    public void setStrategy(CasterVsCreatureStrategy strategem) {
        this.strategy = Optional.of(strategem);
    }

}
