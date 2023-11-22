package com.lhf.game.events.messages.in;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandMessage;

public class CastMessage extends Command {

    CastMessage(String payload) {
        super(CommandMessage.CAST, payload, true);
        this.addPreposition("at");
        this.addPreposition("use");
    }

    @Override
    public Boolean isValid() {
        Boolean indirectsvalid = true;
        if (this.indirects.size() >= 1) {
            indirectsvalid = this.indirects.containsKey("at") || this.indirects.containsKey("use");
        }
        return super.isValid() && this.directs.size() == 1 && indirectsvalid;
    }

    public String getInvocation() {
        if (this.directs.size() < 1) {
            return null;
        }
        return this.directs.get(0);
    }

    public List<String> getTargets() {
        if (!this.indirects.containsKey("at")) {
            return new ArrayList<>();
        }
        List<String> targets = new ArrayList<>();
        String[] splitten = this.indirects.get("at").split(Pattern.quote(", ")); // TODO: test this
        for (String target : splitten) {
            targets.add(target);
        }
        return targets;
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
        sj.add("Targets:");
        if (this.getTargets() != null && this.getTargets().size() > 0) {
            sj.add(this.getTargets().toString());
        } else {
            sj.add("no targets specified");
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
