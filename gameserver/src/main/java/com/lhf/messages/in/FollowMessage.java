package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class FollowMessage extends Command {
    private static String AS_OVERRIDE = "override";
    private static String USE_NULL = "null";

    FollowMessage(String payload) {
        super(CommandMessage.FOLLOW, payload, true);
        this.addPreposition("as");
        this.addPreposition("use");
    }

    @Override
    public Boolean isValid() {
        if (!(super.isValid() && this.directs.size() == 1)) {
            return false;
        }
        if (this.indirects.size() > 2) {
            return false;
        } else if (this.indirects.size() == 0) {
            return true;
        }
        if (this.indirects.size() == 1 && (AS_OVERRIDE.equalsIgnoreCase(this.indirects.get("as"))
                || USE_NULL.equalsIgnoreCase(this.indirects.get("use")))) {
            return true;
        } else if (this.indirects.size() == 2 && (AS_OVERRIDE.equalsIgnoreCase(this.indirects.get("as"))
                && USE_NULL.equalsIgnoreCase(this.indirects.get("use")))) {
            return true;
        }
        return false;
    }

    public String getPersonToFollow() {
        return USE_NULL.equalsIgnoreCase(this.indirects.get("use")) ? null : this.directs.get(0);
    }

    public Boolean isOverride() {
        return this.indirects.size() == 1 && "override".equalsIgnoreCase(this.indirects.get("as"));
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ").add(super.toString());
        sj.add("Override:").add(this.isOverride().toString());
        sj.add("New Leader:").add(this.getPersonToFollow());
        return sj.toString();
    }
}
