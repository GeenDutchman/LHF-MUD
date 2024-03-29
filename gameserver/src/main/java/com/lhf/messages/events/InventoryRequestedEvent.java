package com.lhf.messages.events;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.StringJoiner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.lhf.game.TickType;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.AItem;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.Takeable;
import com.lhf.messages.GameEventType;

public class InventoryRequestedEvent extends GameEvent {
    private final Collection<Takeable> items;
    private final Map<EquipmentSlots, Equipable> equipment;
    private final static TickType tickType = TickType.ACTION;

    public static class Builder extends GameEvent.Builder<Builder> {
        private Collection<Takeable> items = Collections.emptyList();
        private Map<EquipmentSlots, Equipable> equipment = new EnumMap<>(EquipmentSlots.class);

        protected Builder() {
            super(GameEventType.INVENTORY);
        }

        public Collection<Takeable> getItems() {
            return Collections.unmodifiableCollection(items);
        }

        public Builder setItems(Collection<Takeable> items) {
            this.items = items != null ? items : Collections.emptyList();
            return this;
        }

        public Map<EquipmentSlots, Equipable> getEquipment() {
            return Collections.unmodifiableMap(equipment);
        }

        public Builder setEquipment(Map<EquipmentSlots, Equipable> equipment) {
            this.equipment = equipment != null ? equipment : new EnumMap<>(EquipmentSlots.class);
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public InventoryRequestedEvent Build() {
            return new InventoryRequestedEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public InventoryRequestedEvent(Builder builder) {
        super(builder);
        this.items = builder.getItems();
        this.equipment = builder.getEquipment();
    }

    @Override
    public String toString() {
        return this.printString();
    }

    public Collection<Takeable> getItems() {
        return items;
    }

    public Map<EquipmentSlots, Equipable> getEquipment() {
        return equipment;
    }

    @Override
    public TickType getTickType() {
        return tickType;
    }

    @Override
    public Element buildXMLElement(Document nodeGenerator) {
        Element myElement = this.produceContentNode(nodeGenerator);
        if (myElement == null) {
            return myElement;
        }
        Element inventory = nodeGenerator.createElement("Inventory");
        myElement.appendChild(inventory);
        inventory.appendChild(nodeGenerator.createTextNode("INVENTORY:"));
        if ((this.items == null || this.items.isEmpty()) && (this.equipment == null || this.equipment.isEmpty())) {
            inventory.appendChild(nodeGenerator.createTextNode("You have nothing in your inventory."));
            return myElement;
        }

        if (this.items != null) {
            Element itemList = nodeGenerator.createElement("ItemList");
            for (Takeable item : this.items) {
                itemList.appendChild(item.buildXMLElement(nodeGenerator));
            }
            inventory.appendChild(itemList);
        }
        Element equipped = nodeGenerator.createElement("EquippedItems");
        inventory.appendChild(equipped);
        if (this.equipment != null && this.equipment.size() > 0) {
            for (EquipmentSlots slot : EquipmentSlots.values()) {
                Element slotElement = nodeGenerator.createElement("Slot");
                slotElement.setAttribute("slotID", slot.toString());
                slotElement.setIdAttribute("slotID", true);
                slotElement.appendChild(slot.buildXMLElement(nodeGenerator));
                AItem item = this.equipment.get(slot);
                if (item == null) {
                    slotElement.appendChild(nodeGenerator.createTextNode("empty"));
                } else {
                    slotElement.appendChild(item.buildXMLElement(nodeGenerator));
                }
                equipped.appendChild(slotElement);
            }
        } else {
            equipped.appendChild(nodeGenerator.createTextNode("You have nothing equipped."));
        }

        return myElement;
    }

    @Override
    public String printString() {
        StringBuilder sb = new StringBuilder();
        sb.append("INVENTORY: ").append("\n");
        StringJoiner sj = new StringJoiner(", ");
        sj.setEmptyValue("You have nothing in your inventory");
        if (this.items != null) {
            for (Takeable item : this.items) {
                sj.add(item.getName());
            }
        }
        sb.append(sj.toString()).append("\n");
        sj = new StringJoiner(", ");
        sj.setEmptyValue("You have nothing equipped.");
        if (this.equipment != null && this.equipment.size() > 0) {
            for (EquipmentSlots slot : EquipmentSlots.values()) {
                AItem item = this.equipment.get(slot);

                if (item == null) {
                    sj.add(slot.toString() + ": " + "empty. ");
                } else {
                    sj.add(slot.toString() + ": " + item.getName());
                }
            }
        }
        sb.append(sj.toString());

        return sb.toString();
    }

}
