package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.game.map.Directions;
import com.lhf.messages.Command;

public class GoMessage extends CommandAdapter {
    GoMessage(Command command) {
        super(command);
    }

    public Directions getDirection() {
        if (this.getDirects().size() < 1) {
            return null;
        }
        return Directions.getDirections(this.getDirects().get(0));
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("Message:").add(this.getType().toString());
        sj.add("Valid:").add(this.isValid().toString());
        sj.add("Direction:").add(this.getDirection().toString());
        return sj.toString();
    }

}
