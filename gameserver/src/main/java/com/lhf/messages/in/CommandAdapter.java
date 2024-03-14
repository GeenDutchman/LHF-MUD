package com.lhf.messages.in;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.grammar.Prepositions;
import com.lhf.server.interfaces.NotNull;

public abstract class CommandAdapter {
    protected final Command command;

    protected CommandAdapter(@NotNull Command command) {
        if (command == null) {
            throw new IllegalArgumentException("Cannot adapt null command!");
        }
        this.command = command;
    }

    public final Command getCommand() {
        return this.command;
    }

    protected String getWhole() {
        return command.getWhole();
    }

    public AMessageType getType() {
        return command.getType();
    }

    protected List<String> getDirects() {
        return command.getDirects();
    }

    public Boolean isValid() {
        return command.isValid();
    }

    protected List<String> getByPreposition(Prepositions prepositions) {
        return command.getByPreposition(prepositions);
    }

    protected String getFirstByPreposition(Prepositions preposition) {
        return command.getFirstByPreposition(preposition);
    }

    protected String getByPrepositionAsString(Prepositions preposition) {
        return command.getByPrepositionAsString(preposition);
    }

    protected Map<Prepositions, List<String>> getIndirects() {
        return command.getIndirects();
    }

    protected Map<Prepositions, String> getIndirectsAsStrings() {
        return command.getIndirectsAsStrings();
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
