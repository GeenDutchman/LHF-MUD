package com.lhf.game.magic.interfaces;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.lhf.game.creature.Creature;
import com.lhf.game.magic.ISpell;

public abstract class CrowdAffector extends ISpell {
    protected Map<Creature, Creature> targets;

    protected CrowdAffector(Integer level, String name, String description) {
        super(level, name, description);
        this.targets = new HashMap<>();
    }

    public CrowdAffector addTarget(Creature target) {
        this.targets.put(target, target);
        return this;
    }

    public Set<Creature> getTargets() {
        return this.targets.keySet();
    }

}
