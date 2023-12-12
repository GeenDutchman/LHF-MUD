package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class RepeatInMessage extends Command {
    RepeatInMessage(String payload) {
        super(CommandMessage.REPEAT, payload, true);
    }

    @Override
    public Boolean isValid() {
        return super.isValid() && this.directs.size() == 0 && this.indirects.size() == 0;
    }
}
