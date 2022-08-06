package com.lhf.game.creature;

import java.util.List;
import java.util.Map;

import com.lhf.game.EntityEffector;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.Stats;

public interface CreatureEffector extends EntityEffector {

    public Map<Stats, Integer> getStatChanges();

    public Map<Attributes, Integer> getAttributeScoreChanges();

    public Map<Attributes, Integer> getAttributeBonusChanges();

    public abstract List<DamageDice> getDamages();

    public abstract MultiRollResult getDamageResult();

    public abstract void updateDamageResult(MultiRollResult mrr);

    public default boolean isRestoreFaction() {
        return false;
    }

    public default boolean isDeathResult() {
        return false;
    }

    public void setRestoreFaction(boolean restoreFaction);

    public void announceDeath();

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
