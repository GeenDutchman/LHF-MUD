package com.lhf.messages.out;

import java.util.List;
import java.util.StringJoiner;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.Item;
import com.lhf.messages.OutMessageType;

public class EquipOutMessage extends OutMessage {
    public enum EquipResultType {
        SUCCESS, BADSLOT, NOTEQUIPBLE;
    }

    private final EquipResultType subType;
    private final Item item;
    private final String attemptedItemName;
    private final EquipmentSlots attemptedSlot;

    public static class Builder extends OutMessage.Builder<Builder> {
        private EquipResultType subType;
        private Item item;
        private String attemptedItemName;
        private EquipmentSlots attemptedSlot;

        protected Builder() {
            super(OutMessageType.EQUIP);
        }

        public EquipResultType getSubType() {
            return subType;
        }

        public Builder setSubType(EquipResultType type) {
            this.subType = type;
            return this;
        }

        public Item getItem() {
            return item;
        }

        public Builder setItem(Item item) {
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
        public EquipOutMessage Build() {
            return new EquipOutMessage(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public EquipOutMessage(Builder builder) {
        super(builder);
        this.subType = builder.getSubType();
        this.item = builder.getItem();
        this.attemptedItemName = builder.getAttemptedItemName();
        this.attemptedSlot = builder.getAttemptedSlot();
    }

    private String printItemName(String defaultItemName) {
        if (this.item != null) {
            return this.item.getColorTaggedName();
        } else if (this.attemptedItemName != null && !this.attemptedItemName.isBlank()) {
            return "'" + this.attemptedItemName + "'";
        } else if (defaultItemName != null && !defaultItemName.isBlank()) {
            return defaultItemName;
        } else {
            return "item";
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.isBroadcast()) {
            sb.append("Someone ");
            if (this.getSubType() == EquipResultType.SUCCESS) {
                sb.append("equipped ");
            } else {
                sb.append("attempted to equip ");
            }
            sb.append("an item.");
            return sb.toString();
        }
        switch (this.subType) {
            case SUCCESS:
                sb.append("You successfully equipped your ").append(this.printItemName(null));
                if (this.attemptedSlot != null) {
                    sb.append(" to your ").append(this.attemptedSlot.getColorTaggedName()).append(" equiment slot");
                }
                sb.append(".");
                break;
            case BADSLOT:
                if (this.attemptedSlot != null) {
                    sb.append(this.attemptedSlot.getColorTaggedName());
                } else {
                    sb.append("That slot");
                }
                sb.append(" is not an appropriate slot for equipping ");
                sb.append(this.printItemName("that item"));
                sb.append(".");
                if (this.item != null && this.getCorrectSlots().size() > 0) {
                    sb.append("You can equip it to: ");
                    StringJoiner sj = new StringJoiner(", ");
                    for (EquipmentSlots slots : this.getCorrectSlots()) {
                        sj.add(slots.getColorTaggedName());
                    }
                    sb.append(sj.toString());
                }
                break;
            case NOTEQUIPBLE:
                if (this.item != null) {
                    sb.append(this.item.getColorTaggedName());
                } else if (this.attemptedItemName != null) {
                    sb.append("'").append(this.attemptedItemName).append("'");
                } else {
                    sb.append("that");
                }
                sb.append(" is not equippable!");
                break;
            default:
                sb.append("You searched to equip ");
                if (this.attemptedItemName != null && this.attemptedItemName.length() > 0) {
                    sb.append("'").append(this.attemptedItemName).append("' ");
                } else {
                    sb.append("an item ");
                }
                if (this.attemptedSlot != null) {
                    sb.append("to your ").append(this.attemptedSlot.getColorTaggedName()).append(" equipment slot ");
                }
                if (this.item != null) {
                    sb.append(", and found ").append(this.item.getColorTaggedName()).append(" ");
                    if (this.item instanceof Equipable) {
                        sb.append("which could equip to any of these slots: ");
                        StringJoiner sj = new StringJoiner(", ");
                        for (EquipmentSlots slots : this.getCorrectSlots()) {
                            sj.add(slots.getColorTaggedName());
                        }
                        sb.append(sj.toString()).append(". ");
                        if (this.attemptedSlot != null) {
                            sb.append("And you equipped it.");
                        }
                    }
                } else {
                    sb.append(" but did not find such in your inventory. ");
                }
                break;
        }
        return sb.toString();
    }

    @Override
    public String print() {
        return this.toString();
    }

    public EquipResultType getSubType() {
        return subType;
    }

    public Item getItem() {
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
}
