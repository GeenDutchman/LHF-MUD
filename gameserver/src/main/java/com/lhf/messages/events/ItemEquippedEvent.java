package com.lhf.messages.events;

import java.util.List;

import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.game.TickType;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.AItem;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.IItem;
import com.lhf.messages.GameEventType;

public class ItemEquippedEvent extends GameEvent {
    public enum EquipResultType {
        SUCCESS, BADSLOT, NOTEQUIPBLE;
    }

    private final static TickType tickType = TickType.ACTION;
    private final EquipResultType subType;
    private final AItem item;
    private final String attemptedItemName;
    private final EquipmentSlots attemptedSlot;

    public static class Builder extends GameEvent.Builder<Builder> {
        private EquipResultType subType;
        private AItem item;
        private String attemptedItemName;
        private EquipmentSlots attemptedSlot;

        protected Builder() {
            super(GameEventType.EQUIP);
        }

        public EquipResultType getSubType() {
            return subType;
        }

        public Builder setSubType(EquipResultType type) {
            this.subType = type;
            return this;
        }

        public AItem getItem() {
            return item;
        }

        public Builder setItem(AItem item) {
            this.item = item;
            return this;
        }

        public String getAttemptedItemName() {
            return attemptedItemName;
        }

        public Builder setAttemptedItemName(String attemptedItemName) {
            this.attemptedItemName = attemptedItemName;
            return this;
        }

        public EquipmentSlots getAttemptedSlot() {
            return attemptedSlot;
        }

        public Builder setAttemptedSlot(EquipmentSlots attemptedSlot) {
            this.attemptedSlot = attemptedSlot;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public ItemEquippedEvent Build() {
            return new ItemEquippedEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public ItemEquippedEvent(Builder builder) {
        super(builder);
        this.subType = builder.getSubType();
        this.item = builder.getItem();
        this.attemptedItemName = builder.getAttemptedItemName();
        this.attemptedSlot = builder.getAttemptedSlot();
    }

    private void printItemName(RichOutputBuilder builder, String defaultItemName) {
        if (this.item != null) {
            builder.appendTaggable(this.item);
        } else if (this.attemptedItemName != null && !this.attemptedItemName.isBlank()) {
            builder.appendString(this.attemptedItemName, " '", "'");
        } else if (defaultItemName != null && !defaultItemName.isBlank()) {
            builder.appendString(defaultItemName);
        } else {
            builder.appendString("item");
        }
    }

    @Override
    public void buildOutput(RichOutputBuilder builder) {
        if (builder == null) {
            return;
        }
        if (this.isBroadcast()) {
            builder.appendString(String.format("Someone %s an item.",
                    this.getSubType() == EquipResultType.SUCCESS ? "equipped" : "attempted to equip"));
            return;
        }
        if (this.subType == null) {
            builder.appendString(String.format("You searched to equip %s",
                    this.attemptedItemName != null ? "'" + this.attemptedItemName + "'" : "an item"));
            if (this.attemptedSlot != null) {
                builder.appendString("to your");
                builder.appendTaggable(this.attemptedSlot);
                builder.appendString("equipment slot");
            }
            if (this.item != null) {
                builder.appendString(", and found", null, null);
                builder.appendTaggable(this.item);
                if (this.attemptedSlot != null) {
                    builder.appendString("and you equipped it.");
                }
            } else {
                builder.appendString("but did not find such in your inventory.");
            }
            return;
        }
        switch (this.subType) {
        case SUCCESS:
            builder.appendString("You successfully equipped your");
            this.printItemName(builder, null);
            if (this.attemptedSlot != null) {
                builder.appendString("to your");
                builder.appendTaggable(this.attemptedSlot);
                builder.appendString("equipment slot");
            }
            builder.appendString(".", null, null);
            break;
        case BADSLOT:
            if (this.attemptedSlot != null) {
                builder.appendTaggable(attemptedSlot);
            } else {
                builder.appendString("That slot");
            }

            builder.appendString("is not an appropriate slot for equippeing");
            this.printItemName(builder, "that item");
            builder.appendString(".", null, null);
            if (this.item != null && this.getCorrectSlots().size() > 0) {
                builder.appendTaggables(this.getCorrectSlots(), ", ", "You can equip it to:", ".", "No slots");
            }
            break;
        case NOTEQUIPBLE:
            this.printItemName(builder, "that item");
            builder.appendString("is not equippable!");
            break;
        default:
            builder.appendString(String.format("You searched to equip %s",
                    this.attemptedItemName != null ? "'" + this.attemptedItemName + "'" : "an item"));
            if (this.attemptedSlot != null) {
                builder.appendString("to your");
                builder.appendTaggable(this.attemptedSlot);
                builder.appendString("equipment slot");
            }
            if (this.item != null) {
                builder.appendString(", and found", null, null);
                builder.appendTaggable(this.item);
                if (this.attemptedSlot != null) {
                    builder.appendString("and you equipped it.");
                }
            } else {
                builder.appendString("but did not find such in your inventory.");
            }
            break;
        }
    }

    public EquipResultType getSubType() {
        return subType;
    }

    public IItem getItem() {
        return item;
    }

    public String getAttemptedItemName() {
        return attemptedItemName;
    }

    public EquipmentSlots getAttemptedSlot() {
        return attemptedSlot;
    }

    public List<EquipmentSlots> getCorrectSlots() {
        if (this.item != null && this.item instanceof Equipable) {
            return ((Equipable) this.item).getWhichSlots();
        }
        return List.of();
    }

    @Override
    public TickType getTickType() {
        return tickType;
    }
}
