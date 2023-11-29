package com.lhf.messages.in;

import java.util.Optional;
import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class DropMessage extends Command {
    DropMessage(String arguments) {
        super(CommandMessage.DROP, arguments, true);
        this.addPreposition("in");
    }

    @Override
    public Boolean isValid() {
        boolean indirectsvalid = true;
        if (this.indirects.size() >= 1) {
            indirectsvalid = this.indirects.size() == 1 && this.indirects.containsKey("in")
                    && this.indirects.getOrDefault("in", null) != null;
        }
        return super.isValid() && this.directs.size() >= 1 && indirectsvalid;
    }

    public String getTarget() {
        return this.directs.get(0);
    }

    public Optional<String> inContainer() {
        return Optional.ofNullable(this.indirects.getOrDefault("in", null));
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
