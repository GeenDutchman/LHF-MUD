package com.lhf.messages.out;

import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.Item;
import com.lhf.game.item.interfaces.Equipable;
import com.lhf.game.item.interfaces.Takeable;
import com.lhf.messages.OutMessageType;

public class InventoryOutMessage extends OutMessage {
    private Collection<Takeable> items;
    private Map<EquipmentSlots, Equipable> equipment;

    public InventoryOutMessage(Collection<Takeable> items) {
        super(OutMessageType.INVENTORY);
        this.items = items;
    }

    public InventoryOutMessage AddEquipment(Map<EquipmentSlots, Equipable> equipment) {
        this.equipment = equipment;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("INVENTORY: ").append("\n");
        StringJoiner sj = new StringJoiner(", ");
        sj.setEmptyValue("You have nothing in your inventory");
        for (Takeable item : this.items) {
            sj.add(item.getColorTaggedName());
        }
        sb.append(sj.toString()).append("\n");
        sj = new StringJoiner(", ");
        sj.setEmptyValue("You have nothing equipped.");
        if (this.equipment != null && this.equipment.size() > 0) {
            for (EquipmentSlots slot : EquipmentSlots.values()) {
                Item item = this.equipment.get(slot);

                if (item == null) {
                    sj.add(slot.getColorTaggedName() + ": " + "empty. ");
                } else {
                    sj.add(slot.getColorTaggedName() + ": " + item.getColorTaggedName());
                }
            }
        }
        sb.append(sj.toString());

        return sb.toString();
    }

    public Collection<Takeable> getItems() {
        return items;
    }

    public Map<EquipmentSlots, Equipable> getEquipment() {
        return equipment;
    }

}
