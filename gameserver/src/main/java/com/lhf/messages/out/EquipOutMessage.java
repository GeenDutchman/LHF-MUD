package com.lhf.messages.out;

import java.util.List;
import java.util.StringJoiner;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.Item;
import com.lhf.game.item.interfaces.Equipable;
import com.lhf.messages.OutMessageType;

public class EquipOutMessage extends OutMessage {
    public enum EquipResultType {
        SUCCESS, BADSLOT, NOTEQUIPBLE;
    }

    private EquipResultType type;
    private Item item;
    private String attemptedItemName;
    private EquipmentSlots attemptedSlot;

    public EquipOutMessage(Item equipped) {
        super(OutMessageType.EQUIP);
        this.type = EquipResultType.SUCCESS;
        this.item = equipped;
    }

    public EquipOutMessage(EquipResultType type, Item item, String attemptedName, EquipmentSlots attemptedSlot) {
        super(OutMessageType.EQUIP);
        this.type = type;
        this.item = item;
        this.attemptedItemName = attemptedName;
        this.attemptedSlot = attemptedSlot;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        switch (this.type) {
            case SUCCESS:
                sb.append("You successfully equipped your ").append(this.item.getColorTaggedName());
                if (this.attemptedSlot != null) {
                    sb.append(" to your ").append(this.attemptedSlot.getColorTaggedName()).append(" equiment slot.");
                }
                break;
            case BADSLOT:
                sb.append(this.attemptedSlot.getColorTaggedName()).append(" is not an appropriate slot for equipping ");
                if (this.item != null) {
                    sb.append(this.item.getColorTaggedName());
                } else if (this.attemptedItemName != null) {
                    sb.append("'").append(this.attemptedItemName).append("'");
                } else {
                    sb.append("that");
                }
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
                if (this.attemptedItemName != null && this.attemptedItemName.length() > 0) {
                    sb.append("You searched to equip '").append(this.attemptedItemName).append("' ");
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

    public EquipResultType getType() {
        return type;
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
