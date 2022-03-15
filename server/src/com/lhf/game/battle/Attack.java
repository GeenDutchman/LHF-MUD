package com.lhf.game.battle;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class Attack implements Iterable<Map.Entry<String, Integer>> {
    private String attacker;
    private String taggedAttacker;
    private int toHit;
    private Map<String, Integer> flavorAndDamage;

    public Attack(int toHit, String attacker) {
        this.toHit = toHit;
        this.flavorAndDamage = new TreeMap<>();
        this.attacker = attacker;
        this.taggedAttacker = attacker;
    }

    public Attack addFlavorAndDamage(String flavor, int attackDamage) {
        if (this.flavorAndDamage.containsKey(flavor)) {
            this.flavorAndDamage.put(flavor, this.flavorAndDamage.get(flavor) + attackDamage);
        } else {
            this.flavorAndDamage.put(flavor, attackDamage);
        }
        return this;
    }

    public Attack addToHitBonus(int bonus) {
        this.toHit += bonus;
        return this;
    }

    public int getToHit() {
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
    public Iterator<Map.Entry<String, Integer>> iterator() {
        return this.flavorAndDamage.entrySet().iterator();
    }
}
