package com.lhf.game.item.concrete;

import com.lhf.game.dice.*;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.interfaces.Weapon;
import com.lhf.game.item.interfaces.WeaponSubtype;

import java.util.*;

public class RustyDagger extends Weapon {
    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;
    private List<DamageDice> damages;

    public RustyDagger(boolean isVisible) {
        super("Rusty Dagger", isVisible);

        slots = Collections.singletonList(EquipmentSlots.WEAPON);
        types = Arrays.asList(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.DAGGER);
        damages = Arrays.asList(new DamageDice(1, DieType.FOUR, this.getMainFlavor()));
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
    public Map<String, Integer> equip() {
        return new HashMap<>(0); // changes nothing
    }

    @Override
    public Map<String, Integer> unequip() {
        return new HashMap<>(0); // changes nothing
    }

    @Override
    public String getDescription() {
        // sb.append("\r\nAnd best used if you have these proficiencies:
        // ").append(printWhichTypes());
        return "Rusty Dagger to stab monsters with. " + "This can be equipped to: " + printWhichSlots()
        // sb.append("\r\nAnd best used if you have these proficiencies:
        // ").append(printWhichTypes());
        ;
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
