package com.lhf.game.item.interfaces;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.lhf.game.creature.inventory.EquipmentOwner;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.messages.out.SeeOutMessage;

public abstract class Equipable extends Usable {

    public Equipable(String name, boolean isVisible) {
        super(name, isVisible, -1);
    }

    public Equipable(String name, boolean isVisible, int useSoManyTimes) {
        super(name, isVisible, useSoManyTimes);
    }

    public abstract List<EquipmentTypes> getTypes();

    public abstract List<EquipmentSlots> getWhichSlots();

    public abstract Map<String, Integer> getEquippingChanges();

    public String printWhichTypes() {
        StringJoiner sj = new StringJoiner(", ");
        sj.setEmptyValue("no types!");
        for (EquipmentTypes type : this.getTypes()) {
            sj.add(type.toString());
        }
        return sj.toString();
    }

    public String printWhichSlots() {
        StringJoiner sj = new StringJoiner(", ");
        sj.setEmptyValue("no slot!");
        for (EquipmentSlots slot : this.getWhichSlots()) {
            sj.add(slot.toString());
        }
        return sj.toString();
    }

    public String printEquippingChanges() {
        StringJoiner sj = new StringJoiner(", ");
        for (String attr : this.getEquippingChanges().keySet()) {
            sj.add(attr + " would change by " + this.getEquippingChanges().get(attr));
        }
        return sj.toString();
    }

    public String printStats() {
        // TODO: move some of these to the produceMessage
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

    @Override
    public SeeOutMessage produceMessage() {
        SeeOutMessage seeOutMessage = new SeeOutMessage(this);
        return seeOutMessage;
    }

    public Map<String, Integer> onEquippedBy(EquipmentOwner newOwner) {
        return this.getEquippingChanges();
    }

    public Map<String, Integer> onUnequippedBy(EquipmentOwner disowner) {
        Map<String, Integer> undo = new HashMap<String, Integer>(this.getEquippingChanges());
        for (String key : undo.keySet()) {
            undo.put(key, undo.get(key) * -1);
        }
        return undo;
    }
}
