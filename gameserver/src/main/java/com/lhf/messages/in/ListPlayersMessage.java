package com.lhf.messages.in;

import com.lhf.messages.CommandMessage;

public class ListPlayersMessage extends InMessage {

    @Override
    public CommandMessage getType() {
        return CommandMessage.PLAYERS;
    }
}
