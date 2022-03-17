package com.lhf.game.item.concrete;

import com.lhf.game.dice.*;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.interfaces.Weapon;

import java.util.*;

public class BossClub extends Weapon {

    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;
    private List<DamageDice> damages;

    public BossClub(boolean isVisible) {
        super("Boss Club", isVisible);

        slots = Collections.singletonList(EquipmentSlots.WEAPON);
        types = Arrays.asList(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.LONGSWORD);
        damages = Arrays.asList(new DamageDice(2, DieType.EIGHT, this.getMainFlavor()));

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
    public String printWhichTypes() {
        StringJoiner sj = new StringJoiner(", ");
        sj.setEmptyValue("none needed!");
        for (EquipmentTypes type : types) {
            sj.add(type.toString());
        }
        return sj.toString();
    }

    @Override
    public String printWhichSlots() {
        StringJoiner sj = new StringJoiner(", ");
        sj.setEmptyValue("no slot!");
        for (EquipmentSlots slot : slots) {
            sj.add(slot.toString());
        }
        return sj.toString();
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
        return "This is a large club, it seems a bit rusty... wait thats not rust..." +
                "This can be equipped to: " + printWhichSlots();
    }

    @Override
    public DamageFlavor getMainFlavor() {
        return DamageFlavor.BLUDGEONING;
    }

    @Override
    public List<DamageDice> getDamages() {
        return this.damages;
    }

}
