package com.lhf.messages.out;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.Item;
import com.lhf.messages.OutMessageType;

public class UnequipOutMessage extends OutMessage {
    private final Item item;
    private final EquipmentSlots slot;
    private final String attemptedName;

    public static class Builder extends OutMessage.Builder<Builder> {
        private Item item;
        private EquipmentSlots slot;
        private String attemptedName;

        protected Builder() {
            super(OutMessageType.UNEQUIP);
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

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public UnequipOutMessage Build() {
            return new UnequipOutMessage(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public UnequipOutMessage(Builder builder) {
        super(builder);
        this.item = builder.getItem();
        this.slot = builder.getSlot();
        this.attemptedName = builder.getAttemptedName();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.slot != null) {
            if (this.item != null) {
                sb.append("You have unequipped your ").append(this.item.getColorTaggedName());
                sb.append(" from your ").append(this.slot.getColorTaggedName()).append(" equimpent slot");
            } else if (this.attemptedName != null) {
                sb.append("Your equipment slot ").append(this.slot.getColorTaggedName())
                        .append(" does not contain any '").append(this.attemptedName).append("'");
            } else {
                sb.append("Your equipment slot ").append(this.slot.getColorTaggedName()).append(" is empty");
            }
        } else {
            if (this.attemptedName != null) {
                sb.append("'").append(this.attemptedName).append("' is not something that you can unequip right now");
            } else if (this.item != null) {
                sb.append("While ").append(this.item.getColorTaggedName())
                        .append(" is in your inventory, it is not equipped");
            } else {
                sb.append("There's been a problem trying to unequip something here...");
            }
        }
        return sb.append(".").toString();
    }

    public Item getItem() {
        return item;
    }

    public EquipmentSlots getSlot() {
        return slot;
    }

    public String getAttemptedName() {
        return attemptedName;
    }

    @Override
    public String print() {
        return this.toString();
    }
}
