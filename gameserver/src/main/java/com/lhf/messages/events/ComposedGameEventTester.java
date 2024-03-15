package com.lhf.messages.events;

import java.util.Collection;
import java.util.Objects;
import java.util.StringJoiner;

import com.lhf.game.TickType;
import com.lhf.messages.GameEventType;

public class ComposedGameEventTester extends GameEventTester {
    public enum Operation {
        OR, AND, NOT;
    }

    private final Operation operation;
    private final GameEventTester next;

    public ComposedGameEventTester(GameEventType type, Collection<String> contained, Collection<String> notContained,
            TickType tickType, Operation operation, GameEventTester next) {
        super(type, contained, notContained, tickType);
        this.operation = operation != null ? operation : Operation.OR;
        this.next = next;
    }

    public ComposedGameEventTester(GameEventTester first, Operation operation, GameEventTester second) {
        super(first);
        this.operation = operation != null ? operation : Operation.OR;
        this.next = second;
    }

    public Operation getOperation() {
        return operation != null ? this.operation : Operation.OR;
    }

    @Override
    public boolean test(GameEvent argument) {
        boolean firstResult = super.test(argument);
        if (this.next != null) {
            if (this.getOperation() == null) {
                return firstResult || this.next.test(argument);
            }
            switch (this.getOperation()) {
                case AND:
                    return firstResult && this.next.test(argument);
                case NOT:
                    return firstResult && !this.next.test(argument);
                case OR: // fallthrough
                default:
                    return firstResult || this.next.test(argument);

            }
        }
        return firstResult;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(operation, next);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof ComposedGameEventTester))
            return false;
        ComposedGameEventTester other = (ComposedGameEventTester) obj;
        return operation == other.operation && Objects.equals(next, other.next);
    }

    @Override
    public int compareTo(GameEventTester other) {
        if (super.compareTo(other) == 0) {
            return 0;
        }
        if (other instanceof ComposedGameEventTester composed) {
            Operation mine = this.getOperation();
            Operation theirs = composed.getOperation();
            int comparison = mine.compareTo(theirs);
            if (comparison != 0) {
                return comparison;
            }
            if (this.next != null && composed.next != null) {
                return this.next.compareTo(composed.next);
            } else if (this.next != null && composed.next == null) {
                return 1;
            } else if (this.next == null && composed.next != null) {
                return -1;
            } // else if both null
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(super.toString());
        if (this.next != null) {
            sj.add(String.format(" %s %s", this.getOperation(), this.next.toString()));
        }
        return sj.toString();
    }

}
