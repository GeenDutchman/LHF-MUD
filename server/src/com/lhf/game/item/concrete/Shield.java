package com.lhf.game.item.concrete;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.Item;
import com.lhf.game.item.interfaces.Equipable;

import java.util.*;

public class Shield extends Item implements Equipable {
    private int AC = 2;
    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;

    public Shield(boolean isVisible) {
        super("Shield", isVisible);
        types = Collections.singletonList(EquipmentTypes.SHIELD);
        slots = Collections.singletonList(EquipmentSlots.SHIELD);
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
        //sb.append("And best used if you have these proficiencies: ").append(printWhichTypes());
        // tell how much it boosts player?
        return "This is a simple shield, it should protect you a little bit. " + "This can be equipped to: " + printWhichSlots()
                //sb.append("And best used if you have these proficiencies: ").append(printWhichTypes());
                // tell how much it boosts player?
                ;
    }
}
