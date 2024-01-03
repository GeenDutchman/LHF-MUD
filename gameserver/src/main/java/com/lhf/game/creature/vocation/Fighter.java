package com.lhf.game.creature.vocation;

import com.lhf.game.battle.MultiAttacker;
import com.lhf.game.creature.vocation.resourcepools.IntegerResourcePool;
import com.lhf.game.creature.vocation.resourcepools.ResourcePool;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.ResourceCost;

public class Fighter extends Vocation implements MultiAttacker {

    protected class Stamina extends IntegerResourcePool {

        protected Stamina() {
            super(22, level -> level / 2);
        }

        @Override
        public int getLevel() {
            return Fighter.this.level;
        }

    }

    public Fighter() {
        super(VocationName.FIGHTER);
    }

    public Fighter(Integer level) {
        super(VocationName.FIGHTER, level);
    }

    @Override
    public Vocation copy() {
        return new Fighter();
    }

    @Override
    protected ResourcePool initPool() {
        return new Stamina();
    }

    @Override
    public Attributes getAggrovationAttribute() {
        return Attributes.CHA;
    }

    @Override
    public int getAggrovationLevel() {
        return this.level;
    }

    @Override
    public String getMultiAttackerVocation() {
        return this.getName();
    }

    @Override
    public Vocation onRestTick() {
        ResourcePool pool = this.getResourcePool();
        if (pool != null) {
            for (ResourceCost cost : ResourceCost.values()) {
                pool.reload(cost);
            }
        }
        return this;
    }

}
