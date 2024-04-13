package com.lhf.messages.events;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.lhf.OutputBuilder;
import com.lhf.game.TickType;
import com.lhf.game.enums.EquipmentSlots;
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
    public void buildOutput(OutputBuilder builder) {
        if (builder == null) {
            return;
        }

        builder.appendString("INVENTORY", " ", "\r\n");

        if ((this.items == null || this.items.isEmpty()) && (this.equipment == null || this.equipment.isEmpty())) {
            builder.appendString("You have nothing in your inventory.");
            return;
        }

        if (this.items != null && !this.items.isEmpty()) {
            OutputBuilder inventory = builder.produceSubBuilder("Inventory");
            inventory.appendTaggables(this.items);
        }

        if (this.equipment != null && !this.equipment.isEmpty()) {
            OutputBuilder equipped = builder.produceSubBuilder("Equipped");
            for (EquipmentSlots slot : EquipmentSlots.values()) {
                Equipable item = this.equipment.get(slot);
                equipped.appendTaggable(slot, "\r\n", ":");
                if (item != null) {
                    equipped.appendTaggable(item);
                } else {
                    equipped.appendString("empty");
                }
            }
        } else {
            builder.appendString("You have nothing equipped.");
        }
    }

}
