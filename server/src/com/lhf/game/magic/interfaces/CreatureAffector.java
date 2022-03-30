package com.lhf.game.magic.interfaces;

import com.lhf.game.creature.Creature;
import com.lhf.game.magic.ISpell;

public abstract class CreatureAffector extends ISpell {
    protected Creature target;

    protected CreatureAffector(Integer level, String name, String description) {
        super(level, name, description);
        this.target = null;
    }

    public Creature getTarget() {
        return target;
    }

    public CreatureAffector setTarget(Creature target) {
        this.target = target;
        return this;
    }

}
