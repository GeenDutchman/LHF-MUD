package com.lhf.game.events.messages.in;

import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandMessage;

public class StatusMessage extends Command {

    StatusMessage(String payload) {
        super(CommandMessage.STATUS, payload, true);
    }

    @Override
    public Boolean isValid() {
        return super.isValid() && this.directs.size() == 0 && this.indirects.size() == 0;
    }

}
