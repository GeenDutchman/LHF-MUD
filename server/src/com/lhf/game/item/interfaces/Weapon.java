package com.lhf.game.item.interfaces;

import java.util.List;
import java.util.StringJoiner;

import com.lhf.game.battle.Attack;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.enums.DamageFlavor;

public abstract class Weapon extends Usable implements Equipable {
    public Weapon(String name, boolean isVisible) {
        super(name, isVisible, -1);
    }

    public abstract List<DamageDice> getDamages();

    public String printWhichDamages() {
        StringJoiner sj = new StringJoiner(", ");
        sj.setEmptyValue("no damage!");
        for (DamageDice dd : this.getDamages()) {
            sj.add(dd.toString());
        }
        return sj.toString();
    }

    public Attack modifyAttack(Attack attack) {
        for (DamageDice dd : this.getDamages()) {
            attack = attack.addFlavorAndRoll(dd.getFlavor(), dd.rollDice());
        }
        return attack;
    }

    public abstract DamageFlavor getMainFlavor();

    public abstract WeaponSubtype getSubType();

    @Override
    public String printStats() {
        StringBuilder sb = new StringBuilder();
        if (this.getDamages().size() > 0) {
            sb.append("This weapon deals damage like ").append(this.printWhichDamages()).append("\n");
        }
        return sb.append(Equipable.super.printStats()).toString();
    }
}
