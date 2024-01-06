package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.grammar.Prepositions;

public class FollowMessage extends CommandAdapter {
    private static String AS_OVERRIDE = "override";
    private static String USE_NULL = "null";

    FollowMessage(Command command) {
        super(command);
    }

    public String getPersonToFollow() {
        return USE_NULL.equalsIgnoreCase(this.getIndirects().get(Prepositions.USE)) ? null : this.getDirects().get(0);
    }

    public Boolean isOverride() {
        return this.getIndirects().size() == 1
                && AS_OVERRIDE.equalsIgnoreCase(this.getIndirects().get(Prepositions.AS));
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ").add(super.toString());
        sj.add("Override:").add(this.isOverride().toString());
        sj.add("New Leader:").add(this.getPersonToFollow());
        return sj.toString();
    }
}
