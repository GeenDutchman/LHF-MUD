package com.lhf.game.battle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.lhf.Taggable;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffector;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.interfaces.Weapon;

public class Attack implements CreatureEffector {
    // TODO: add aggro
    private Creature attacker;
    private Weapon weapon;
    private MultiRollResult toHit;
    private Map<Stats, Integer> statChanges;
    private Map<Attributes, Integer> attributeScoreChanges;
    private Map<Attributes, Integer> attributeBonusChanges;
    private List<DamageDice> damages;
    private MultiRollResult damageDone;
    private boolean restoreFaction;
    private boolean deathResult;

    public Attack(Creature attacker, Weapon weapon) {
        this.attacker = attacker;
        this.weapon = weapon;
        this.statChanges = new TreeMap<>();
        this.attributeScoreChanges = new TreeMap<>();
        this.attributeBonusChanges = new TreeMap<>();
        this.damages = new ArrayList<>(weapon.getDamages());
        this.damageDone = null;
        this.restoreFaction = false;
        this.deathResult = false;
        this.calculateHitAndDamage();
    }

    private void calculateHitAndDamage() {
        int attributeBonus = 0;
        AttributeBlock retrieved = this.attacker.getAttributes();
        Integer str = retrieved.getMod(Attributes.STR);
        Integer dex = retrieved.getMod(Attributes.DEX);
        switch (weapon.getSubType()) {
            case CREATUREPART:
                // fallthrough
            case FINESSE:
                if (dex > str) {
                    attributeBonus = dex;
                    this.toHit = this.attacker.check(Attributes.DEX);
                } else {
                    attributeBonus = str;
                    this.toHit = this.attacker.check(Attributes.STR);
                }
                break;
            case PRECISE:
                attributeBonus = dex;
                this.toHit = this.attacker.check(Attributes.DEX);
                break;
            case MARTIAL:
                // fallthrough
            default:
                attributeBonus = str;
                this.toHit = this.attacker.check(Attributes.STR);
                break;
        }
        this.toHit.addBonus(attributeBonus);
        for (DamageDice dd : this.getDamages()) {
            if (this.damageDone == null) {
                this.damageDone = new MultiRollResult(dd.rollDice());
            } else {
                this.damageDone.addResult(dd.rollDice());
            }
        }
        if (this.damageDone != null) {
            this.damageDone.addBonus(attributeBonus);
        }
    }

    public Attack addToHitBonus(int bonus) {
        this.toHit.addBonus(bonus);
        return this;
    }

    public MultiRollResult getToHit() {
        return toHit;
    }

    public Creature getAttacker() {
        return attacker;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public Attack setAttacker(Creature attacker) {
        this.attacker = attacker;
        return this;
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

    public List<DamageDice> getDamages() {
        return damages;
    }

    @Override
    public MultiRollResult getDamageResult() {
        return this.damageDone;
    }

    @Override
    public void updateDamageResult(MultiRollResult mrr) {
        this.damageDone = mrr;
    }

    public boolean isRestoreFaction() {
        return restoreFaction;
    }

    public boolean isDeathResult() {
        return deathResult;
    }

    @Override
    public void setRestoreFaction(boolean restoreFaction) {
        this.restoreFaction = restoreFaction;
    }

    @Override
    public void announceDeath() {
        this.deathResult = true;
    }

    @Override
    public Taggable getGeneratedBy() {
        return this.weapon;
    }

    @Override
    public EffectPersistence getPersistence() {
        return EffectPersistence.INSTANT;
    }
}
