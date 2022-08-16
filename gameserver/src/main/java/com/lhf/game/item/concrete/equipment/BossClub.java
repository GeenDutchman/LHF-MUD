package com.lhf.game.item.concrete.equipment;

import java.util.List;

import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.Weapon;
import com.lhf.game.item.interfaces.WeaponSubtype;

public class BossClub extends Weapon {

    public BossClub(boolean isVisible) {
        super("Boss Club", isVisible, DamageFlavor.BLUDGEONING, WeaponSubtype.MARTIAL);

        this.slots = List.of(EquipmentSlots.WEAPON);
        this.types = List.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.LONGSWORD);
        this.damages = List.of(new DamageDice(2, DieType.EIGHT, this.getMainFlavor()));
        this.descriptionString = "This is a large club, it seems a bit rusty... wait that is not rust...\n";

    }

}
