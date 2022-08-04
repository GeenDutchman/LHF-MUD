package com.lhf.game.creature;

import java.util.Map;

import com.lhf.game.EntityEffector;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.Stats;

public interface CreatureEffector extends EntityEffector {

    public Map<Stats, Integer> getStatChanges();

    public Map<Attributes, Integer> getAttributeScoreChanges();

    public Map<Attributes, Integer> getAttributeBonusChanges();

    public Map<DamageFlavor, RollResult> getDamages();

    public boolean isRestoreFaction();

    public boolean isDeathResult();

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

    // combines whatever damage is of that flavor, if it exists. Otherwise it is
    // set.
    public default CreatureEffector addDamage(DamageFlavor flavor, RollResult rollResult) {
        if (this.getDamages().containsKey(flavor)) {
            this.getDamages().get(flavor).combine(rollResult);
        } else {
            this.getDamages().put(flavor, rollResult);
        }
        return this;
    }

}
