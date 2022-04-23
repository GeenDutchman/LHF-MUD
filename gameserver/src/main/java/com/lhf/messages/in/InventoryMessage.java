package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class InventoryMessage extends Command {

    InventoryMessage(String payload) {
        super(CommandMessage.INVENTORY, payload, true);
    }

    @Override
    public Boolean isValid() {
        return super.isValid() && this.directs.size() == 0 && this.indirects.size() == 0;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.toString());
        return sj.toString();
    }

}
