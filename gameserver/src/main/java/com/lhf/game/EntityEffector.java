package com.lhf.game;

import com.lhf.Taggable;
import com.lhf.game.creature.Creature;

public interface EntityEffector extends Comparable<EntityEffector> {
    public enum EffectPersistence {
        INSTANT, DURATION;
    }

    public Creature creatureResponsible();

    public Taggable getGeneratedBy();

    public EffectPersistence getPersistence();

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
