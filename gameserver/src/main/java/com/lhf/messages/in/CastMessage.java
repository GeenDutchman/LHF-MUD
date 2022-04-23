package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class CastMessage extends Command {

    CastMessage(String payload) {
        super(CommandMessage.CAST, payload, true);
        this.addPreposition("at");
        this.addPreposition("use");
    }

    @Override
    public Boolean isValid() {
        Boolean indirectsvalid = true;
        if (this.indirects.size() == 1) {
            indirectsvalid = this.indirects.containsKey("at") || this.indirects.containsKey("use");
        } else if (this.indirects.size() == 2) {
            indirectsvalid = this.indirects.containsKey("at") && this.indirects.containsKey("use");
        } else if (this.indirects.size() > 2) {
            indirectsvalid = false;
        }
        return super.isValid() && this.directs.size() == 1 && indirectsvalid;
    }

    public String getInvocation() {
        if (this.directs.size() < 1) {
            return null;
        }
        return this.directs.get(0);
    }

    public String getTarget() {
        return this.indirects.getOrDefault("at", null); // TODO: allow for target selection
    }

    public Integer getLevel() {
        String value = this.indirects.getOrDefault("use", null);
        if (value == null) {
            return null;
        }
        return Integer.valueOf(value);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("Message:").add(this.getType().toString());
        sj.add("Valid:").add(this.isValid().toString());
        sj.add("Invocation:");
        if (this.getInvocation() != null) {
            sj.add(this.getInvocation());
        } else {
            sj.add("No invocation!");
        }
        sj.add("Target:");
        if (this.getTarget() != null) {
            sj.add(this.getTarget());
        } else {
            sj.add("no target");
        }
        sj.add("Level:");
        if (this.getLevel() != null && this.getLevel() >= 0) {
            sj.add(this.getLevel().toString());
        } else {
            sj.add("default level");
        }
        return sj.toString();
    }

}
