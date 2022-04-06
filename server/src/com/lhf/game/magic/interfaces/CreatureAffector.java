package com.lhf.game.magic.interfaces;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.lhf.game.creature.Creature;
import com.lhf.game.magic.ISpell;

public abstract class CreatureAffector extends ISpell {
    protected Map<Creature, Creature> targets;
    protected Boolean singleTarget;

    protected CreatureAffector(Integer level, String name, String description, Boolean singleTarget) {
        super(level, name, description);
        this.targets = new HashMap<>();
        this.singleTarget = singleTarget;
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

}
