package com.lhf.game.map.objects.item.interfaces;

import com.lhf.game.Attack;

public interface Weapon extends Equipable, Usable {
    WeaponSubtype getWeaponSubtype();

    int rollToHit();

    int rollDamage();

    Attack rollAttack();
}
