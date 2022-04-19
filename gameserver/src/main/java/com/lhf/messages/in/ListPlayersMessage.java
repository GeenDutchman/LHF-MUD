package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class ListPlayersMessage extends Command {

    protected ListPlayersMessage(String payload) {
        super(CommandMessage.PLAYERS, payload, true);
    }

}
