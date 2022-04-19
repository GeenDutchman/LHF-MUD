package com.lhf.messages.in;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class UnequipMessage extends Command {
    private String possibleWeapon;

    UnequipMessage(String args) {
        super(CommandMessage.UNEQUIP, args, true);
        this.addPreposition("to");
    }

    public EquipmentSlots getEquipSlot() {
        String strSlot = this.getByPreposition("to");
        if (strSlot == null) {
            return null;
        }
        return EquipmentSlots.valueOf(strSlot);
    }

    public String getPossibleWeapon() {
        return possibleWeapon;
    }

}
