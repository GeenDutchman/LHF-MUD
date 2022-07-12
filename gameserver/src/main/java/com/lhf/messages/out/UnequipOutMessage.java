package com.lhf.messages.out;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.Item;

public class UnequipOutMessage extends OutMessage {
    private Item item;
    private EquipmentSlots slot;
    private String attemptedName;

    public UnequipOutMessage(EquipmentSlots slot, Item item) {
        this.item = item;
        this.slot = slot;
    }

    public UnequipOutMessage(EquipmentSlots slot, String attemptedName) {
        this.slot = slot;
        this.attemptedName = attemptedName;
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
}
