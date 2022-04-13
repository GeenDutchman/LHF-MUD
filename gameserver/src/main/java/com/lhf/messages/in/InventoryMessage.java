package com.lhf.messages.in;

import com.lhf.messages.CommandMessage;

public class InventoryMessage extends InMessage {

    @Override
    public CommandMessage getType() {
        return CommandMessage.INVENTORY;
    }
}
