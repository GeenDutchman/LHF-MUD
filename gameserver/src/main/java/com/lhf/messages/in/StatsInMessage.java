package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class StatsInMessage extends Command {

    StatsInMessage(String whole) {
        super(CommandMessage.STATS, whole, true);
    }

}
