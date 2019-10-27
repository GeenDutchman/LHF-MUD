package com.lhf.game.map.objects.item.interfaces;

import com.lhf.game.Attack;

public interface IWeapon extends IEquipable {
    WeaponSubtype getWeaponSubtype();

    int rollAttack();

    int getDamage();

    Attack getAttack();
}
