package com.lhf.game.battle;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.enums.DamageFlavor;

public class Attack implements Iterable<Map.Entry<DamageFlavor, RollResult>> {
    private String attacker;
    private String taggedAttacker;
    private RollResult toHit;
    private Map<DamageFlavor, RollResult> flavorAndDamage;

    public Attack(RollResult toHit, String attacker) {
        this.toHit = toHit;
        this.flavorAndDamage = new TreeMap<>();
        this.attacker = attacker;
        this.taggedAttacker = attacker;
    }

    public Attack addFlavorAndRoll(DamageFlavor flavor, RollResult attackDamage) {
        if (this.flavorAndDamage.containsKey(flavor)) {
            this.flavorAndDamage.put(flavor, this.flavorAndDamage.get(flavor).combine(attackDamage));
        } else {
            this.flavorAndDamage.put(flavor, attackDamage);
        }
        return this;
    }

    // Will first try to add bonus to `flavor` and if that isn't there, will try to
    // add it to any one flavor. Else, no change.
    public Attack addDamageBonus(DamageFlavor flavor, int bonus) {
        if (this.flavorAndDamage.containsKey(flavor)) {
            this.flavorAndDamage.put(flavor, this.flavorAndDamage.get(flavor).addBonus(bonus));
        } else if (this.flavorAndDamage.size() > 0) {
            for (DamageFlavor iterFlavor : this.flavorAndDamage.keySet()) {
                this.flavorAndDamage.put(iterFlavor, this.flavorAndDamage.get(iterFlavor).addBonus(bonus));
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

    public String getAttacker() {
        return attacker;
    }

    public String getTaggedAttacker() {
        return taggedAttacker;
    }

    public Attack setAttacker(String attacker) {
        this.attacker = attacker;
        return this;
    }

    public Attack setTaggedAttacker(String taggedAttacker) {
        this.taggedAttacker = taggedAttacker;
        return this;
    }

    @Override
    public Iterator<Map.Entry<DamageFlavor, RollResult>> iterator() {
        return this.flavorAndDamage.entrySet().iterator();
    }
}
