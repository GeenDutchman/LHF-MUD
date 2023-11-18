package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class UnequipMessage extends Command {

    UnequipMessage(String args) {
        super(CommandMessage.UNEQUIP, args, true);
    }

    public String getUnequipWhat() {
        if (this.directs.size() < 1) {
            return null;
        }
        return this.directs.get(0);
    }

    @Override
    public Boolean isValid() {
        return super.isValid() && this.directs.size() >= 1 && this.indirects.size() == 0;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.toString());
        sj.add("UnequipWhat:");
        String unequip = this.getUnequipWhat();
        if (unequip != null) {
            sj.add(unequip);
        } else {
            sj.add("Nothing to unequip!");
        }
        sj.add("IsSlot:").add(EquipmentSlots.isEquipmentSlot(unequip).toString());
        return sj.toString();
    }

}
