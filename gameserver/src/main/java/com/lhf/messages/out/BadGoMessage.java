package com.lhf.messages.out;

import java.util.Collection;
import java.util.StringJoiner;

import com.lhf.game.map.Directions;
import com.lhf.messages.OutMessageType;

public class BadGoMessage extends OutMessage {
    public enum BadGoType {
        DNE, BLOCKED, NO_ROOM;
    }

    private BadGoType type;
    private Directions attempted;
    private Collection<Directions> available;

    public BadGoMessage(BadGoType type, Directions attempted) {
        super(OutMessageType.BAD_GO);
        this.type = type;
        this.attempted = attempted;
        this.available = null;
    }

    public BadGoMessage(BadGoType type, Directions attempted, Collection<Directions> available) {
        super(OutMessageType.BAD_GO);
        this.type = type;
        this.attempted = attempted;
        this.available = available;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("You cannot go ");
        if (this.attempted != null) {
            sb.append(this.attempted.getColorTaggedName());
        } else {
            sb.append("that way");
        }
        sb.append(". ");
        if (this.type == BadGoType.DNE || this.attempted == null) {
            sb.append("That way is a wall. ");
        } else if (this.type == BadGoType.BLOCKED) {
            sb.append("Your path is blocked ");
        } else if (this.type == BadGoType.NO_ROOM) {
            sb.append("You are not in a room. ");
        }
        if (this.available != null && this.available.size() > 0) {
            if (this.available.size() == 1 && this.attempted != null && this.type == BadGoType.BLOCKED) {
                sb.append("No other directions are available.  Try finding a way to unblock it. ");
            } else {
                sb.append("You could try to go one of:");
                StringJoiner sj = new StringJoiner(", ");
                for (Directions s : this.available) {
                    if (!(this.type == BadGoType.BLOCKED && s.equals(this.attempted))) {
                        sj.add(s.getColorTaggedName());
                    }
                }
                sb.append(sj.toString());
            }
        } else {
            sb.append("No directions are available.");
        }
        return sb.toString();
    }

    public Directions getAttempted() {
        return attempted;
    }

    public Collection<Directions> getAvailable() {
        return available;
    }

}
