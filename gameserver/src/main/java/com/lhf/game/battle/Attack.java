package com.lhf.game.battle;

import java.util.Iterator;
import java.util.Set;

import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.item.Weapon;

public class Attack implements Iterable<CreatureEffect> {
    private ICreature attacker;
    private Weapon weapon;
    private Set<CreatureEffect> effects;

    public Attack(ICreature attacker, Weapon weapon, Set<CreatureEffect> effects) {
        this.attacker = attacker;
        this.weapon = weapon;
        this.effects = Set.copyOf(effects);
    }

    public ICreature getAttacker() {
        return attacker;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public Attack setAttacker(ICreature attacker) {
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
