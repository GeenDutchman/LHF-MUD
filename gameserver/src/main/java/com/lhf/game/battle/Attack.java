package com.lhf.game.battle;

import java.util.Iterator;
import java.util.Set;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.item.Weapon;

public class Attack implements Iterable<CreatureEffect> {
    // TODO: add aggro
    private Creature attacker;
    private Weapon weapon;
    private Set<CreatureEffect> effects;

    public Attack(Creature attacker, Weapon weapon, Set<CreatureEffect> effects) {
        this.attacker = attacker;
        this.weapon = weapon;
        this.effects = Set.copyOf(effects);
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

    public Set<CreatureEffect> getEffects() {
        return this.effects;
    }

    @Override
    public Iterator<CreatureEffect> iterator() {
        return this.getEffects().iterator();
    }
}
