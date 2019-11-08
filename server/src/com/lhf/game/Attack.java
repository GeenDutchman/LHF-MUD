package com.lhf.game;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class Attack implements Iterable {
    protected int toHit;
    protected Map<String, Integer> flavorAndDamage;

    public Attack(int toHit) {
        this.toHit = toHit;
        this.flavorAndDamage = new TreeMap<>();
    }

    public Attack addFlavorAndDamage(String flavor, int attackDamage) {
        if (this.flavorAndDamage.containsKey(flavor)) {
            this.flavorAndDamage.put(flavor, this.flavorAndDamage.get(flavor) + attackDamage);
        } else {
            this.flavorAndDamage.put(flavor, attackDamage);
        }
        return this;
    }

    @NotNull
    @Override
    public Iterator iterator() {
        return this.flavorAndDamage.entrySet().iterator();
    }

    public Integer getDamage(String flavor) {
        return flavorAndDamage.get(flavor);
    }
}
