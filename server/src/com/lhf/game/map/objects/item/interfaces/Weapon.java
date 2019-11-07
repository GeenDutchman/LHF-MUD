package com.lhf.game.map.objects.item.interfaces;

import com.lhf.game.Attack;

public abstract class Weapon extends Usable implements Equipable {
    public Weapon(String name, boolean isVisible) {
        super(name, isVisible, -1);
    }
//    List<EquipmentTypes> getWeaponSubtype();

    public abstract int rollToHit();

    public abstract int rollDamage();

    public abstract Attack rollAttack();
}
