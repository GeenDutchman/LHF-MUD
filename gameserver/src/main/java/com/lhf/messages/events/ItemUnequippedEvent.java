package com.lhf.messages.events;

import com.lhf.game.TickType;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.IItem;
import com.lhf.game.item.Item;
import com.lhf.messages.GameEventType;
import com.lhf.messages.ITickEvent;

public class ItemUnequippedEvent extends GameEvent implements ITickEvent {
    public enum UnequipResultType {
        SUCCESS, ITEM_NOT_EQUIPPED, ITEM_NOT_FOUND;
    }

    private final static TickType tickType = TickType.ACTION;

    private final UnequipResultType subType;
    private final Item item;
    private final EquipmentSlots slot;
    private final String attemptedName;

    public static class Builder extends GameEvent.Builder<Builder> {
        private UnequipResultType subType;
        private Item item;
        private EquipmentSlots slot;
        private String attemptedName;

        protected Builder() {
            super(GameEventType.UNEQUIP);
        }

        public Item getItem() {
            return item;
        }

        public Builder setItem(Item item) {
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

    private String describeItem() {
        if (this.item != null) {
            return this.item.getColorTaggedName();
        } else if (this.attemptedName != null && !this.attemptedName.isBlank()) {
            return this.attemptedName;
        } else {
            return "item";
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.isBroadcast()) {
            sb.append("Someone ");
            if (this.subType == UnequipResultType.SUCCESS) {
                sb.append("has unequipped");
            } else {
                sb.append("attempted to unequip");
            }
            sb.append("an item.");
            return sb.toString();
        }
        if (this.subType == null) {
            sb.append("You tried to unequip an item ");
            if (this.attemptedName != null && !this.attemptedName.isBlank()) {
                sb.append("with the name of ").append(this.attemptedName);
            }
            if (this.item != null) {
                sb.append("and an item ").append(this.item.getColorTaggedName()).append(" was found");
            }
        } else {
            switch (this.subType) {
                case SUCCESS:
                    sb.append("You have unequipped your ").append(this.describeItem());
                    break;
                case ITEM_NOT_EQUIPPED:
                    sb.append("Your ").append(this.describeItem()).append(" is not equipped");
                    break;
                case ITEM_NOT_FOUND:
                    sb.append("That ").append(this.describeItem()).append(" was not found");
                    break;
                default:
                    sb.append("You tried to unequip an item ");
                    if (this.attemptedName != null && !this.attemptedName.isBlank()) {
                        sb.append("with the name of ").append(this.attemptedName);
                    }
                    if (this.item != null) {
                        sb.append("and an item ").append(this.item.getColorTaggedName()).append(" was found");
                    }
                    break;
            }
        }
        if (this.slot != null) {
            sb.append(" in your ").append(this.slot.getColorTaggedName()).append(" equipment slot");
        }
        sb.append(".");
        return sb.toString();
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
    public String print() {
        return this.toString();
    }
}
