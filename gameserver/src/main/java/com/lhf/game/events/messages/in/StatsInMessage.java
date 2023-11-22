package com.lhf.game.events.messages.in;

import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandMessage;

public class StatsInMessage extends Command {

    StatsInMessage(String whole) {
        super(CommandMessage.STATS, whole, true);
    }

}
