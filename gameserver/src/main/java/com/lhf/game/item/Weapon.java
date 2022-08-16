package com.lhf.game.item;

import java.util.ArrayList;
import java.util.List;

import com.lhf.game.battle.Attack;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.item.interfaces.WeaponSubtype;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.messages.out.SeeOutMessage.SeeCategory;

public class Weapon extends Equipable {
    protected List<DamageDice> damages;
    protected DamageFlavor mainFlavor;
    protected WeaponSubtype subtype;

    public Weapon(String name, boolean isVisible, DamageFlavor mainFlavor, WeaponSubtype subtype) {
        super(name, isVisible, -1);
        this.damages = new ArrayList<>();
        this.mainFlavor = mainFlavor;
        this.subtype = subtype;
    }

    public List<DamageDice> getDamages() {
        return this.damages;
    }

    public Attack modifyAttack(Attack attack) {
        return attack;
    }

    public DamageFlavor getMainFlavor() {
        return this.mainFlavor;
    }

    public WeaponSubtype getSubType() {
        return this.subtype;
    }

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
