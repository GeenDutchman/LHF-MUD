package com.lhf.messages.in;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.grammar.Prepositions;

public class TakeMessage extends CommandAdapter {

    public TakeMessage(Command command) {
        super(command);
    }

    public String getTarget() {
        if (this.getDirects().size() < 1) {
            return null;
        }
        return this.getDirects().get(0);
    }

    public List<String> getTargets() {
        return this.getDirects();
    }

    public Optional<String> fromContainer() {
        return Optional.ofNullable(this.getIndirects().getOrDefault(Prepositions.FROM, null));
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.toString());
        sj.add("Object:");
        String thing = this.getTarget();
        if (thing != null) {
            sj.add(thing);
        } else {
            sj.add("Nothing!");
        }
        Optional<String> container = this.fromContainer();
        if (container.isPresent()) {
            sj.add("From:").add(container.get());
        }
        return sj.toString();
    }
}
