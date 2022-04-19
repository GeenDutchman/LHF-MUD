package com.lhf.messages.in;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

import static com.lhf.game.enums.EquipmentSlots.*;

public class EquipMessage extends Command {
    private String itemName = "";

    EquipMessage(String args) {
        super(CommandMessage.EQUIP, args, true);
        this.addPreposition("to");
    }

    public String getItemName() {
        return itemName;
    }

    public EquipmentSlots getEquipSlot() {
        String strSlot = this.getByPreposition("to");
        if (strSlot == null) {
            return null;
        }
        return EquipmentSlots.valueOf(strSlot);
    }

}
