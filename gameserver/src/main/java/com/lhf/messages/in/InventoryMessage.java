package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class InventoryMessage extends Command {

    InventoryMessage(String payload) {
        super(CommandMessage.INVENTORY, payload, true);
    }

}
