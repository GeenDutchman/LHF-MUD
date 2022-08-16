package com.lhf.game.item.concrete.equipment;

import java.util.List;

import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.interfaces.Weapon;
import com.lhf.game.item.interfaces.WeaponSubtype;

public class RustyDagger extends Weapon {
    private List<DamageDice> damages;

    public RustyDagger(boolean isVisible) {
        super("Rusty Dagger", isVisible);

        slots = List.of(EquipmentSlots.WEAPON);
        types = List.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.DAGGER);
        damages = List.of(new DamageDice(1, DieType.FOUR, this.getMainFlavor()));
        this.descriptionString = "Rusty Dagger to stab monsters with. \n";
    }

    @Override
    public DamageFlavor getMainFlavor() {
        return DamageFlavor.PIERCING;
    }

    @Override
    public List<DamageDice> getDamages() {
        return this.damages;
    }

    @Override
    public WeaponSubtype getSubType() {
        return WeaponSubtype.PRECISE;
    }
}
