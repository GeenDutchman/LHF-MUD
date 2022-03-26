package com.lhf.game.item.interfaces;

import com.lhf.game.creature.inventory.EquipmentOwner;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public interface Equipable extends Takeable {

    List<EquipmentTypes> getTypes();

    List<EquipmentSlots> getWhichSlots();

    Map<String, Integer> getEquippingChanges();

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

    default String printEquippingChanges() {
        StringJoiner sj = new StringJoiner(", ");
        for (String attr : this.getEquippingChanges().keySet()) {
            sj.add(attr + " would change by " + this.getEquippingChanges().get(attr));
        }
        return sj.toString();
    }

    default String printStats() {
        StringBuilder sb = new StringBuilder();
        if (this.getEquippingChanges().size() > 0) {
            sb.append("When equipped: ").append(this.printEquippingChanges()).append("\n");
        }
        if (this.getWhichSlots().size() > 0) {
            sb.append("This can be equipped to: ").append(this.printWhichSlots()).append("\n");
        }
        if (this.getTypes().size() > 0) {
            sb.append("This can be best used with the proficiencies: ").append(this.printWhichTypes()).append("\n");
        }
        return sb.toString();
    }

    default Map<String, Integer> onEquippedBy(EquipmentOwner newOwner) {
        return this.getEquippingChanges();
    }

    default Map<String, Integer> onUnequippedBy(EquipmentOwner disowner) {
        Map<String, Integer> undo = new HashMap<String, Integer>(this.getEquippingChanges());
        for (String key : undo.keySet()) {
            undo.put(key, undo.get(key) * -1);
        }
        return undo;
    }
}
