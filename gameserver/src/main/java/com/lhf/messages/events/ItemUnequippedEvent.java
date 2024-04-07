package com.lhf.messages.events;

import com.lhf.OutputBuilder;
import com.lhf.game.TickType;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.AItem;
import com.lhf.game.item.IItem;
import com.lhf.messages.GameEventType;

public class ItemUnequippedEvent extends GameEvent {
    public enum UnequipResultType {
        SUCCESS, ITEM_NOT_EQUIPPED, ITEM_NOT_FOUND;
    }

    private final static TickType tickType = TickType.ACTION;

    private final UnequipResultType subType;
    private final AItem item;
    private final EquipmentSlots slot;
    private final String attemptedName;

    public static class Builder extends GameEvent.Builder<Builder> {
        private UnequipResultType subType;
        private AItem item;
        private EquipmentSlots slot;
        private String attemptedName;

        protected Builder() {
            super(GameEventType.UNEQUIP);
        }

        public AItem getItem() {
            return item;
        }

        public Builder setItem(AItem item) {
            this.item = item;
            return this;
        }

        public EquipmentSlots getSlot() {
            return slot;
        }

        public Builder setSlot(EquipmentSlots slot) {
            this.slot = slot;
            return this;
        }

        public String getAttemptedName() {
            return attemptedName;
        }

        public Builder setAttemptedName(String attemptedName) {
            this.attemptedName = attemptedName;
            return this;
        }

        public UnequipResultType getSubType() {
            return subType;
        }

        public Builder setSubType(UnequipResultType subType) {
            this.subType = subType;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public ItemUnequippedEvent Build() {
            return new ItemUnequippedEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public ItemUnequippedEvent(Builder builder) {
        super(builder);
        this.subType = builder.getSubType();
        this.item = builder.getItem();
        this.slot = builder.getSlot();
        this.attemptedName = builder.getAttemptedName();
    }

    private void describeItem(OutputBuilder builder) {
        if (this.item != null) {
            builder.appendTaggable(item);
        } else if (this.attemptedName != null && !this.attemptedName.isBlank()) {
            builder.appendString(attemptedName);
        } else {
            builder.appendString("item");
        }
    }

    @Override
    public String toString() {
        return this.printString();
    }

    public IItem getItem() {
        return item;
    }

    public EquipmentSlots getSlot() {
        return slot;
    }

    public String getAttemptedName() {
        return attemptedName;
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
        if (this.isBroadcast()) {
            builder.appendString(String.format("Someone %s an item",
                    UnequipResultType.SUCCESS.equals(this.subType) ? "has unequipped" : "attempted to unequip"));
            return;

        }
        if (this.subType == null) {
            builder.appendString("You tried to unequip an item ");
            if (this.attemptedName != null && !this.attemptedName.isBlank()) {
                builder.appendString(String.format("with the name of %s ", this.attemptedName));
            }
            if (this.item != null) {
                builder.appendString("and an item ");
                builder.appendTaggable(item);
                builder.appendString(" was found");
            }
        } else {
            switch (this.subType) {
                case SUCCESS:
                    builder.appendString("You have unequipped your");
                    this.describeItem(builder);
                    break;
                case ITEM_NOT_EQUIPPED:
                    builder.appendString("Your ");
                    this.describeItem(builder);
                    builder.appendString(" is not equipped");
                    break;
                case ITEM_NOT_FOUND:
                    builder.appendString("That ");
                    this.describeItem(builder);
                    builder.appendString(" was not found");
                    break;
                default:
                    builder.appendString("You tried to unequip an item ");
                    if (this.attemptedName != null && !this.attemptedName.isBlank()) {
                        builder.appendString(String.format("with the name of %s ", this.attemptedName));
                    }
                    if (this.item != null) {
                        builder.appendString("and an item ");
                        builder.appendTaggable(item);
                        builder.appendString(" was found");
                    }
                    break;
            }
        }
        if (this.slot != null) {
            builder.appendString(" in your ");
            builder.appendTaggable(this.slot);
            builder.appendString(" equipment slot");
        }
        builder.appendString(".", null, null);
    }

}
