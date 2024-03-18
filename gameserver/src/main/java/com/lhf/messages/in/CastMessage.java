package com.lhf.messages.in;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.grammar.Prepositions;

public class CastMessage extends CommandAdapter {

    public CastMessage(Command command) {
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
        targets.addAll(this.getIndirects().getOrDefault(Prepositions.AT, List.of())); // TODO: test this

        return targets;
    }

    public Integer getLevel() {
        String firstLevel = this.getFirstByPreposition(Prepositions.USE);
        if (firstLevel == null || firstLevel.isBlank()) {
            return null;
        }
        return Integer.valueOf(firstLevel);
    }

    public List<String> getMetadata(Prepositions prepositions) {
        return this.getByPreposition(prepositions);
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
