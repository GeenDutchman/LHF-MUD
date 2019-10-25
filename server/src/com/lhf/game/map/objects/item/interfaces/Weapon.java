package com.lhf.game.map.objects.item.interfaces;

public interface Weapon extends Equipable {
    WeaponSubtype getWeaponSubtype();

    int rollAttack();
}
