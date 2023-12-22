package com.lhf.messages.out;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.StringJoiner;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.Item;
import com.lhf.game.item.Takeable;
import com.lhf.messages.GameEventType;

public class InventoryOutMessage extends GameEvent {
    private final Collection<Takeable> items;
    private final Map<EquipmentSlots, Equipable> equipment;

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
        public InventoryOutMessage Build() {
            return new InventoryOutMessage(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public InventoryOutMessage(Builder builder) {
        super(builder);
        this.items = builder.getItems();
        this.equipment = builder.getEquipment();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("INVENTORY: ").append("\n");
        StringJoiner sj = new StringJoiner(", ");
        sj.setEmptyValue("You have nothing in your inventory");
        if (this.items != null) {
            for (Takeable item : this.items) {
                sj.add(item.getColorTaggedName());
            }
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

    @Override
    public String print() {
        return this.toString();
    }

}
