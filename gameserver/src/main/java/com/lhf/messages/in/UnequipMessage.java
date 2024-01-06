package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.messages.Command;

public class UnequipMessage extends CommandAdapter {

    UnequipMessage(Command command) {
        super(command);
    }

    public String getUnequipWhat() {
        if (this.getDirects().size() < 1) {
            return null;
        }
        return this.getDirects().get(0);
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
