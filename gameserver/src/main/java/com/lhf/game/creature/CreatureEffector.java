package com.lhf.game.creature;

import java.util.Map;
import java.util.TreeMap;

import com.lhf.game.EntityEffector;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.Stats;

public class CreatureEffector extends EntityEffector {
    private Map<Stats, Integer> statChanges;
    private Map<Attributes, Integer> attributeScoreChanges;
    private Map<Attributes, Integer> attributeBonusChanges;
    private Map<DamageFlavor, RollResult> damages;
    private boolean restoreFaction;

    public CreatureEffector(String generatedBy, EffectPersistence persistence) {
        super(generatedBy, persistence);
        this.statChanges = new TreeMap<>();
        this.attributeScoreChanges = new TreeMap<>();
        this.attributeBonusChanges = new TreeMap<>();
        this.damages = new TreeMap<>();
        this.restoreFaction = false;
    }

    public Map<Stats, Integer> getStatChanges() {
        return statChanges;
    }

    public Map<Attributes, Integer> getAttributeScoreChanges() {
        return attributeScoreChanges;
    }

    public Map<Attributes, Integer> getAttributeBonusChanges() {
        return attributeBonusChanges;
    }

    public Map<DamageFlavor, RollResult> getDamages() {
        return damages;
    }

    public boolean isRestoreFaction() {
        return restoreFaction;
    }

    // replaces whatever value was in `stats`, if it existed
    public CreatureEffector addStatChange(Stats stats, Integer delta) {
        this.statChanges.put(stats, delta);
        return this;
    }

    // replaces whatever value was in `attr`, if it existed
    public CreatureEffector addAttributeScoreChange(Attributes attr, Integer delta) {
        this.attributeScoreChanges.put(attr, delta);
        return this;
    }

    // replaces whatever value was in `attr`, if it existed
    public CreatureEffector addAttributeBonusChange(Attributes attr, Integer delta) {
        this.attributeBonusChanges.put(attr, delta);
        return this;
    }

    // combines whatever damage is of that flavor, if it exists. Otherwise it is
    // set.
    public CreatureEffector addDamage(DamageFlavor flavor, RollResult rollResult) {
        if (this.damages.containsKey(flavor)) {
            this.damages.get(flavor).combine(rollResult);
        } else {
            this.damages.put(flavor, rollResult);
        }
        return this;
    }

    public void setRestoreFaction(boolean restoreFaction) {
        this.restoreFaction = restoreFaction;
    }

}
