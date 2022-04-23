package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class StatusMessage extends Command {

    StatusMessage(String payload) {
        super(CommandMessage.STATUS, payload, true);
    }

    @Override
    public Boolean isValid() {
        return super.isValid() && this.directs.size() == 0 && this.indirects.size() == 0;
    }

}
