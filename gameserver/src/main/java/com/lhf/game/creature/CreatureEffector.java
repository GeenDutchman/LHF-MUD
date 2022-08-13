package com.lhf.game.creature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.lhf.Taggable;
import com.lhf.game.EffectPersistence;
import com.lhf.game.EntityEffector;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.Stats;

public interface CreatureEffector extends EntityEffector {

    public class BasicCreatureEffector extends EntityEffector.BasicEntityEffector implements CreatureEffector {
        protected Map<Stats, Integer> statChanges;

        protected Map<Attributes, Integer> attributeScoreChanges;

        protected Map<Attributes, Integer> attributeBonusChanges;

        protected List<DamageDice> damages;

        protected MultiRollResult damageResult;

        protected boolean restoreFaction;

        public BasicCreatureEffector(Creature creatureResponsible, Taggable generatedBy,
                EffectPersistence persistence) {
            super(creatureResponsible, generatedBy, persistence);
            this.statChanges = new TreeMap<>();
            this.attributeScoreChanges = new TreeMap<>();
            this.attributeBonusChanges = new TreeMap<>();
            this.damages = new ArrayList<>();
            this.damageResult = null;
            this.restoreFaction = false;
        }

        @Override
        public Map<Stats, Integer> getStatChanges() {
            return this.statChanges;
        }

        @Override
        public Map<Attributes, Integer> getAttributeScoreChanges() {
            return this.attributeScoreChanges;
        }

        @Override
        public Map<Attributes, Integer> getAttributeBonusChanges() {
            return this.attributeBonusChanges;
        }

        @Override
        public List<DamageDice> getDamages() {
            return this.damages;
        }

        @Override
        public MultiRollResult getDamageResult() {
            if (this.damageResult == null) {
                for (DamageDice dd : this.getDamages()) {
                    if (this.damageResult == null) {
                        this.damageResult = new MultiRollResult(dd.rollDice());
                    } else {
                        this.damageResult.addResult(dd.rollDice());
                    }
                }
            }
            return this.damageResult;
        }

        @Override
        public void updateDamageResult(MultiRollResult mrr) {
            this.damageResult = mrr;
        }
    }

    public Map<Stats, Integer> getStatChanges();

    public Map<Attributes, Integer> getAttributeScoreChanges();

    public Map<Attributes, Integer> getAttributeBonusChanges();

    public abstract List<DamageDice> getDamages();

    public abstract MultiRollResult getDamageResult();

    public abstract void updateDamageResult(MultiRollResult mrr);

    public default boolean isRestoreFaction() {
        return false;
    }

    // replaces whatever value was in `stats`, if it existed
    public default CreatureEffector addStatChange(Stats stats, Integer delta) {
        this.getStatChanges().put(stats, delta);
        return this;
    }

    // replaces whatever value was in `attr`, if it existed
    public default CreatureEffector addAttributeScoreChange(Attributes attr, Integer delta) {
        this.getAttributeScoreChanges().put(attr, delta);
        return this;
    }

    // replaces whatever value was in `attr`, if it existed
    public default CreatureEffector addAttributeBonusChange(Attributes attr, Integer delta) {
        this.getAttributeScoreChanges().put(attr, delta);
        return this;
    }

    public default CreatureEffector addDamage(DamageDice damageDice) {
        this.getDamages().add(damageDice);
        return this;
    }

    public default CreatureEffector addDamageBonus(int bonus) {
        if (this.getDamageResult() != null) {
            this.getDamageResult().addBonus(bonus);
        }
        return this;
    }

}
