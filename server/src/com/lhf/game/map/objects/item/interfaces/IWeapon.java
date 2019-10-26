package com.lhf.game.map.objects.item.interfaces;

public interface IWeapon extends IEquipable {
    WeaponSubtype getWeaponSubtype();

    int rollAttack();
}
