package com.lhf.game.item.concrete.equipment;

import java.util.List;

import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.Weapon;
import com.lhf.game.item.interfaces.WeaponSubtype;

public class RustyDagger extends Weapon {

    public RustyDagger(boolean isVisible) {
        super("Rusty Dagger", isVisible, DamageFlavor.PIERCING, WeaponSubtype.PRECISE);

        this.slots = List.of(EquipmentSlots.WEAPON);
        this.types = List.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.DAGGER);
        this.damages = List.of(new DamageDice(1, DieType.FOUR, this.getMainFlavor()));
        this.descriptionString = "Rusty Dagger to stab monsters with. \n";
    }

}
