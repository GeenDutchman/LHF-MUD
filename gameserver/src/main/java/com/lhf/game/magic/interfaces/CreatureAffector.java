package com.lhf.game.magic.interfaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.lhf.game.battle.BattleAction;
import com.lhf.game.creature.Creature;
import com.lhf.game.magic.ISpell;
import com.lhf.game.magic.strategies.CasterVsCreatureStrategy;

public abstract class CreatureAffector extends ISpell implements BattleAction {
    protected Map<Creature, Creature> targets;
    protected Boolean singleTarget;
    protected Optional<CasterVsCreatureStrategy> strategy;

    protected CreatureAffector(Integer level, String name, String description, Boolean singleTarget) {
        super(level, name, description);
        this.targets = new HashMap<>();
        this.singleTarget = singleTarget;
        this.strategy = Optional.empty();
    }

    @Override
    public CreatureAffector addTarget(Creature target) {
        if (this.singleTarget && this.targets.size() > 0) {
            this.targets.clear();
        }
        this.targets.put(target, target);
        return this;
    }

    @Override
    public BattleAction addTargets(Collection<Creature> targets) {
        if (this.singleTarget && this.targets.size() > 0 && targets.size() > 0) {
            this.targets.clear();
            Creature first = (Creature) targets.toArray()[0];
            this.targets.put(first, first);
        } else {
            for (Creature creature : targets) {
                this.targets.put(creature, creature);
            }
        }
        return null;
    }

    @Override
    public boolean hasTargets() {
        return this.targets.size() > 0;
    }

    @Override
    public List<Creature> getTargets() {
        return new ArrayList<Creature>(this.targets.keySet());
    }

    public Optional<CasterVsCreatureStrategy> getStrategy() {
        return this.strategy;
    }

    public void setStrategy(CasterVsCreatureStrategy strategem) {
        this.strategy = Optional.of(strategem);
    }

}
