package com.lhf.messages.out;

import java.util.List;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.Item;
import com.lhf.game.item.interfaces.Equipable;

public class EquipOutMessage extends OutMessage {
    public enum EquipResultType {
        SUCCESS, BADSLOT, NOTEQUIPBLE;
    }

    private EquipResultType type;
    private Item item;
    private OutMessage unequipMessage;
    private String attemptedItemName;
    private EquipmentSlots attemptedSlot;

    public EquipOutMessage(OutMessage unequipMessage, Item equipped) {
        this.type = EquipResultType.SUCCESS;
        this.unequipMessage = unequipMessage;
        this.item = equipped;
    }

    public EquipOutMessage(EquipResultType type, Item item, String attemptedName, EquipmentSlots attemptedSlot) {
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
                if (this.unequipMessage != null) {
                    sb.append(this.unequipMessage.toString()).append(" ");
                }
                sb.append("You successfully equipped your ").append(this.item.getColorTaggedName());
                sb.append(" to your ").append(this.attemptedSlot.getColorTaggedName()).append(" equiment slot.");
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
                if (this.item != null && this.item instanceof Equipable) {
                    sb.append("You can equip it to: ").append(((Equipable) this.item).printWhichSlots());
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
                        sb.append("which could equip to any of these slots: ")
                                .append(((Equipable) this.item).printWhichSlots()).append(". ");
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

    public OutMessage getUnequipMessage() {
        return unequipMessage;
    }

    public String getAttemptedItemName() {
        return attemptedItemName;
    }

    public EquipmentSlots getAttemptedSlot() {
        return attemptedSlot;
    }
}
