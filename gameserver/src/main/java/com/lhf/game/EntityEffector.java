package com.lhf.game;

import com.lhf.Taggable;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.EffectPersistence.Ticker;
import com.lhf.game.creature.Creature;

public interface EntityEffector extends Comparable<EntityEffector> {

    public class BasicEntityEffector implements EntityEffector {
        protected Creature creatureResponsible;
        protected Taggable generatedBy;
        protected EffectPersistence persistence;
        protected Ticker ticker;

        public BasicEntityEffector(Creature creatureResponsible, Taggable generatedBy, EffectPersistence persistence) {
            this.creatureResponsible = creatureResponsible;
            this.generatedBy = generatedBy;
            this.persistence = persistence;
            this.ticker = null;
        }

        @Override
        public Creature creatureResponsible() {
            return this.creatureResponsible;
        }

        @Override
        public Taggable getGeneratedBy() {
            return this.generatedBy;
        }

        @Override
        public EffectPersistence getPersistence() {
            return this.persistence;
        }

        @Override
        public Ticker getTicker() {
            if (this.ticker == null) {
                this.ticker = this.persistence.getTicker();
            }
            return this.ticker;
        }

    }

    public Creature creatureResponsible();

    public Taggable getGeneratedBy();

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
