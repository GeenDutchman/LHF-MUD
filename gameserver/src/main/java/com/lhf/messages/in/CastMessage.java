package com.lhf.messages.in;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import com.lhf.messages.Command;
import com.lhf.messages.grammar.Prepositions;

public class CastMessage extends CommandAdapter {

    CastMessage(Command command) {
        super(command);
    }

    public String getInvocation() {
        if (this.getDirects().size() < 1) {
            return null;
        }
        return this.getDirects().get(0);
    }

    public List<String> getTargets() {
        if (!this.getIndirects().containsKey(Prepositions.AT)) {
            return new ArrayList<>();
        }
        List<String> targets = new ArrayList<>();
        String[] splitten = this.getIndirects().get(Prepositions.AT).split(Pattern.quote(", ")); // TODO: test this
        for (String target : splitten) {
            targets.add(target);
        }
        return targets;
    }

    public Integer getLevel() {
        String value = this.getIndirects().getOrDefault(Prepositions.USE, null);
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
