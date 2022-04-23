package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class EquipMessage extends Command {
    EquipMessage(String args) {
        super(CommandMessage.EQUIP, args, true);
        this.addPreposition("to");
    }

    @Override
    public Boolean isValid() {
        Boolean validated = true;
        if (this.indirects.size() > 1) {
            validated = false;
        } else {
            if (this.indirects.containsKey("to")) {
                validated = this.directs.size() == 1 && EquipmentSlots.isEquipmentSlot(this.getByPreposition("to"));
            } else {
                validated = false;
            }
        }
        return super.isValid() && this.directs.size() >= 1 && validated;
    }

    public String getItemName() {
        if (this.directs.size() < 1) {
            return null;
        }
        return this.directs.get(0);
    }

    public EquipmentSlots getEquipSlot() {
        String strSlot = this.getByPreposition("to");
        if (strSlot == null) {
            return null;
        }
        return EquipmentSlots.getEquipmentSlot(strSlot);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("Message:").add(this.getType().toString());
        sj.add("Valid:").add(this.isValid().toString());
        sj.add("What:");
        if (this.getItemName() != null) {
            sj.add(this.getItemName());
        } else {
            sj.add("equipping nothing!");
        }
        sj.add("To:");
        if (this.getEquipSlot() != null) {
            sj.add(this.getEquipSlot().toString());
        } else {
            sj.add("default slot");
        }
        return sj.toString();
    }
}
