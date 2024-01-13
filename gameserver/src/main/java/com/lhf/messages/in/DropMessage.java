package com.lhf.messages.in;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.grammar.Prepositions;

public class DropMessage extends CommandAdapter {
    public DropMessage(Command command) {
        super(command);
    }

    public String getTarget() {
        return this.getDirects().get(0);
    }

    public List<String> getTargets() {
        return this.getDirects();
    }

    public Optional<String> inContainer() {
        return Optional.ofNullable(this.getIndirects().getOrDefault(Prepositions.IN, null));
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("Message:").add(this.getType().toString());
        sj.add("Valid:").add(this.isValid().toString());
        sj.add("What:");
        if (this.getTarget() != null) {
            sj.add(this.getTarget());
        } else {
            sj.add("not dropping anything!");
        }
        Optional<String> container = this.inContainer();
        if (container.isPresent()) {
            sj.add("In:").add(container.get());
        }
        return sj.toString();
    }

}
