package com.lhf.game.item.concrete;

import com.lhf.game.dice.*;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.interfaces.Weapon;
import com.lhf.game.item.interfaces.WeaponSubtype;

import java.util.*;

public class Longsword extends Weapon {

    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;
    private List<DamageDice> damages;

    public Longsword(boolean isVisible) {
        super("Longsword", isVisible);

        slots = Collections.singletonList(EquipmentSlots.WEAPON);
        types = Arrays.asList(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.LONGSWORD);
        damages = Arrays.asList(new DamageDice(1, DieType.EIGHT, this.getMainFlavor()));
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
        // sb.append("And best used if you have these proficiencies:
        // ").append(printWhichTypes());
        // TODO: should this describe that it does 1d6 damage?
        return "This is a nice, long, shiny sword.  It's a bit simple though..." +
                "This can be equipped to: " + printWhichSlots();
    }

    @Override
    public DamageFlavor getMainFlavor() {
        return DamageFlavor.SLASHING;
    }

    @Override
    public List<DamageDice> getDamages() {
        return this.damages;
    }

    @Override
    public WeaponSubtype getSubType() {
        return WeaponSubtype.MARTIAL;
    }

}
