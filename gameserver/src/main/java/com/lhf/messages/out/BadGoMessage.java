package com.lhf.messages.out;

import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

import com.lhf.game.map.Directions;

public class BadGoMessage extends OutMessage {
    private Directions attempted;
    private Collection<String> available; // TODO: change this to a list of directions

    public BadGoMessage(Directions attempted) {
        this.attempted = attempted;
        this.available = null;
    }

    public BadGoMessage(Directions attempted, Collection<String> available) {
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
        if (this.available != null && this.available.size() > 0) {
            sb.append("You could try to go one of:");
            StringJoiner sj = new StringJoiner(", ");
            for (String s : this.available) {
                sj.add(s);
            }
            sb.append(sj.toString());
        } else {
            sb.append("No directions are available.");
        }
        return sb.toString();
    }

    public Directions getAttempted() {
        return attempted;
    }

    public List<String> getAvailable() {
        return available;
    }

}
