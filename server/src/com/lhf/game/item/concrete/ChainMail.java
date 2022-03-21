package com.lhf.game.item.concrete;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.Item;
import com.lhf.game.item.interfaces.Equipable;

import java.util.*;

public class ChainMail extends Item implements Equipable {
    private int AC = 5;

    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;

    public ChainMail(boolean isVisible) {
        super("Chain Mail", isVisible);

        slots = Collections.singletonList(EquipmentSlots.ARMOR);
        types = Arrays.asList(EquipmentTypes.HEAVYARMOR);
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
        Map<String, Integer> result = new HashMap<>();
        result.put("AC", this.AC);
        return result;
    }

    @Override
    public Map<String, Integer> unequip() {
        Map<String, Integer> result = new HashMap<>();
        result.put("AC", -1 * this.AC);
        return result;
    }

    @Override
    public String getDescription() {
        return "This is some heavy chainmail. " + "It looks protective... now if only it wasn't so heavy\n" +
                this.printStats();
    }
}
