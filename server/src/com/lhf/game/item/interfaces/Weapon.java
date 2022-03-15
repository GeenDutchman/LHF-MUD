package com.lhf.game.item.interfaces;

import com.lhf.game.battle.Attack;

public abstract class Weapon extends Usable implements Equipable {
    public Weapon(String name, boolean isVisible) {
        super(name, isVisible, -1);
    }

    public abstract int rollToHit();

    public abstract int rollDamage();

    public abstract Attack rollAttack();

    public abstract String getMainFlavor();
}
