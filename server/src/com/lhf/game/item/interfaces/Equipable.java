package com.lhf.game.item.interfaces;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public interface Equipable extends Takeable {

    List<EquipmentTypes> getTypes();

    List<EquipmentSlots> getWhichSlots();

    default String printWhichTypes() {
        StringJoiner sj = new StringJoiner(", ");
        sj.setEmptyValue("no types!");
        for (EquipmentTypes type : this.getTypes()) {
            sj.add(type.toString());
        }
        return sj.toString();
    }

    default String printWhichSlots() {
        StringJoiner sj = new StringJoiner(", ");
        sj.setEmptyValue("no slot!");
        for (EquipmentSlots slot : this.getWhichSlots()) {
            sj.add(slot.toString());
        }
        return sj.toString();
    }

    default String printStats() {
        StringBuilder sb = new StringBuilder();
        if (this.getWhichSlots().size() > 0) {
            sb.append("This can be equipped to: ").append(this.printWhichSlots()).append("\n");
        }
        if (this.getTypes().size() > 0) {
            sb.append("This can be best used with the proficiencies: ").append(this.printWhichTypes()).append("\n");
        }
        return sb.toString();
    }

    Map<String, Integer> equip();

    Map<String, Integer> unequip();
}
