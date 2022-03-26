package com.lhf.game.item.concrete;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.interfaces.Weapon;
import com.lhf.game.item.interfaces.WeaponSubtype;

public class RustyDagger extends Weapon {
    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;
    private List<DamageDice> damages;
    private Map<String, Integer> equippingChanges;

    public RustyDagger(boolean isVisible) {
        super("Rusty Dagger", isVisible);

        slots = Collections.singletonList(EquipmentSlots.WEAPON);
        types = Arrays.asList(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.DAGGER);
        damages = Arrays.asList(new DamageDice(1, DieType.FOUR, this.getMainFlavor()));
        equippingChanges = new HashMap<>(0); // changes nothing
    }

    @Override
    public List<EquipmentTypes> getTypes() {
        return types;
    }

    @Override
    public List<EquipmentSlots> getWhichSlots() {
        return slots;
    }

    @Override
    public Map<String, Integer> getEquippingChanges() {
        return this.equippingChanges;
    }

    @Override
    public String getDescription() {
        return "Rusty Dagger to stab monsters with. \n" + this.printStats();
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
