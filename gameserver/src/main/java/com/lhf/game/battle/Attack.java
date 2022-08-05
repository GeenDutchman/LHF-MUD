package com.lhf.game.battle;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.lhf.Taggable;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffector;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.interfaces.Weapon;

public class Attack implements CreatureEffector, Iterable<Map.Entry<DamageFlavor, RollResult>> {
    // TODO: add aggro
    private Creature attacker;
    private Weapon weapon;
    private RollResult toHit;
    private Map<Stats, Integer> statChanges;
    private Map<Attributes, Integer> attributeScoreChanges;
    private Map<Attributes, Integer> attributeBonusChanges;
    private Map<DamageFlavor, RollResult> damages;
    private boolean restoreFaction;
    private boolean deathResult;

    public Attack(Creature attacker, Weapon weapon) {
        this.attacker = attacker;
        this.weapon = weapon;
        this.statChanges = new TreeMap<>();
        this.attributeScoreChanges = new TreeMap<>();
        this.attributeBonusChanges = new TreeMap<>();
        this.damages = new TreeMap<>();
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
        this.weapon.modifyAttack(this);
        this.addDamageBonus(this.weapon.getMainFlavor(), attributeBonus);
    }

    public Attack addFlavorAndRoll(DamageFlavor flavor, RollResult attackDamage) {
        CreatureEffector.super.addDamage(flavor, attackDamage);
        return this;
    }

    // Will first try to add bonus to `flavor` and if that isn't there, will try to
    // add it to any one flavor. Else, no change.
    public Attack addDamageBonus(DamageFlavor flavor, int bonus) {
        if (this.getDamages().containsKey(flavor)) {
            this.getDamages().put(flavor, this.getDamages().get(flavor).addBonus(bonus));
        } else if (this.getDamages().size() > 0) {
            for (DamageFlavor iterFlavor : this.getDamages().keySet()) {
                this.getDamages().put(iterFlavor, this.getDamages().get(iterFlavor).addBonus(bonus));
                break;
            }
        }
        return this;
    }

    public Attack addToHitBonus(int bonus) {
        this.toHit.addBonus(bonus);
        return this;
    }

    public RollResult getToHit() {
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

    @Override
    public Iterator<Map.Entry<DamageFlavor, RollResult>> iterator() {
        return this.getDamages().entrySet().iterator();
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
