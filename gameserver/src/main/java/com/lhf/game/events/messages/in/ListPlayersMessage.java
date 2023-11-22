package com.lhf.game.events.messages.in;

import java.util.StringJoiner;

import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandMessage;

public class ListPlayersMessage extends Command {

    ListPlayersMessage(String payload) {
        super(CommandMessage.PLAYERS, payload, true);
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