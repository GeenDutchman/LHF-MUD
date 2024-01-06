package com.lhf.messages.in;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.grammar.Prepositions;

abstract class CommandAdapter {
    protected final Command command;

    protected CommandAdapter(Command command) {
        this.command = command;
    }

    public final Command getCommand() {
        return this.command;
    }

    protected String getWhole() {
        return command.getWhole();
    }

    public CommandMessage getType() {
        return command.getType();
    }

    protected List<String> getDirects() {
        return command.getDirects();
    }

    public Boolean isValid() {
        return command.isValid();
    }

    @Deprecated(forRemoval = true)
    public List<String> getWhat() {
        return command.getWhat();
    }

    protected String getByPreposition(Prepositions preposition) {
        return command.getByPreposition(preposition);
    }

    protected Map<Prepositions, String> getIndirects() {
        return command.getIndirects();
    }

    @Override
    public int hashCode() {
        return command.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return command.equals(obj);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("Message:").add(this.getType().toString());
        sj.add("Valid:").add(this.isValid().toString());
        sj.add("Payload:").add(this.getWhole());
        return this.command.toString();
    }

    public static Optional<Command> fromString(String payload) {
        if (payload == null || payload.length() == 0) {
            return Optional.empty();
        }

        return Optional.of(Command.parse(payload));
    }

}
