package com.lhf.game.item.interfaces;

import java.util.List;

import com.lhf.game.battle.Attack;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.enums.DamageFlavor;

public abstract class Weapon extends Usable implements Equipable {
    public Weapon(String name, boolean isVisible) {
        super(name, isVisible, -1);
    }

    public abstract List<DamageDice> getDamages();

    public Attack modifyAttack(Attack attack) {
        for (DamageDice dd : this.getDamages()) {
            attack = attack.addFlavorAndDamage(dd.getFlavor(), dd.roll());
        }
        return attack;
    }

    public abstract DamageFlavor getMainFlavor();
}
