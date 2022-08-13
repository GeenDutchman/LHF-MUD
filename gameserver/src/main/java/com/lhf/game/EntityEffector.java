package com.lhf.game;

import com.lhf.Taggable;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.EffectPersistence.Ticker;
import com.lhf.game.creature.Creature;

public interface EntityEffector extends Comparable<EntityEffector> {
    public Creature creatureResponsible();

    public Taggable getGeneratedBy();

    public String getName();

    public EffectPersistence getPersistence();

    public Ticker getTicker();

    public default int tick(TickType type) {
        return this.getTicker().tick(type);
    }

    @Override
    public default int compareTo(EntityEffector o) {
        if (this.equals(o)) {
            return 0;
        }
        int namecompare = this.getGeneratedBy().getColorTaggedName().compareTo(o.getGeneratedBy().getColorTaggedName());
        if (namecompare != 0) {
            return namecompare;
        }
        return this.getPersistence().compareTo(o.getPersistence());
    }

}
