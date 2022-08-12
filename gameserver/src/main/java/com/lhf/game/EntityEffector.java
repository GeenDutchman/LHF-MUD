package com.lhf.game;

import com.lhf.Taggable;
import com.lhf.game.creature.Creature;

public interface EntityEffector extends Comparable<EntityEffector> {
    public enum EffectPersistence {
        INSTANT, ONE_ACTION, TWO_ACTIONS, THREE_ACTIONS, BATTLE, ONE_ROOM, TWO_ROOM, THREE_ROOM, CONDITIONAL;

        public EffectPersistence tick() {
            switch (this) {
                case CONDITIONAL:
                    return CONDITIONAL;
                case THREE_ROOM:
                    return TWO_ROOM;
                case TWO_ROOM:
                    return ONE_ROOM:
                case ONE_ROOM:
                    return null;
                case BATTLE:
                    return null;
                case THREE_ACTIONS:
                    return TWO_ACTIONS;
                case TWO_ACTIONS:
                    return ONE_ACTION;
                case ONE_ACTION:
                    return null;
                case INSTANT:
                    return null;            
                default:
                    return ONE_ACTION;
            }
        }
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
