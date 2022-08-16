package com.lhf.game.item.interfaces;

import java.util.List;

import com.lhf.game.battle.Attack;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.messages.out.SeeOutMessage.SeeCategory;

public abstract class Weapon extends Equipable {
    public Weapon(String name, boolean isVisible) {
        super(name, isVisible, -1);
    }

    public abstract List<DamageDice> getDamages();

    public Attack modifyAttack(Attack attack) {
        return attack;
    }

    public abstract DamageFlavor getMainFlavor();

    public abstract WeaponSubtype getSubType();

    @Override
    public SeeOutMessage produceMessage() {
        SeeOutMessage seeOutMessage = super.produceMessage();
        if (this.getDamages() == null) {
            return seeOutMessage;
        }
        for (DamageDice dd : this.getDamages()) {
            seeOutMessage.addSeen(SeeCategory.DAMAGES, dd);
        }
        return seeOutMessage;
    }

}
