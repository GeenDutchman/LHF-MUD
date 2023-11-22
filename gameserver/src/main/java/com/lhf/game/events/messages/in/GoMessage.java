package com.lhf.game.events.messages.in;

import java.util.StringJoiner;

import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandMessage;
import com.lhf.game.map.Directions;

public class GoMessage extends Command {
    GoMessage(String payload) {
        super(CommandMessage.GO, payload, true);
    }

    public Directions getDirection() {
        if (this.directs.size() < 1) {
            return null;
        }
        return Directions.getDirections(this.directs.get(0));
    }

    @Override
    public Boolean isValid() {
        return super.isValid() && this.directs.size() == 1 && Directions.isDirections(this.directs.get(0))
                && this.indirects.size() == 0;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("Message:").add(this.getGameEventType().toString());
        sj.add("Valid:").add(this.isValid().toString());
        sj.add("Direction:").add(this.getDirection().toString());
        return sj.toString();
    }

}
