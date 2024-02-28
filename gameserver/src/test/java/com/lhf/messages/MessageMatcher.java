package com.lhf.messages;

import java.util.Collection;
import java.util.List;

import org.mockito.ArgumentMatcher;

import com.lhf.game.TickType;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.GameEventTester;

public class MessageMatcher extends GameEventTester implements ArgumentMatcher<GameEvent> {
    protected boolean printIt = false;
    protected String sentTo = "";

    public MessageMatcher(GameEventType type, Collection<String> contained, Collection<String> notContained,
            TickType tickType) {
        super(type, contained, notContained, tickType);
    }

    public MessageMatcher(GameEventType type, List<String> containedWords, List<String> notContainedWords) {
        this(type, containedWords, notContainedWords, null);
    }

    public MessageMatcher(GameEventType type, String contained) {
        this(type, List.of(contained), null, null);
    }

    public MessageMatcher(GameEventType type) {
        this(type, null, null, null);
    }

    public MessageMatcher(String contained) {
        this(null, List.of(contained), null, null);
    }

    public MessageMatcher(MessageMatcher other) {
        super(other);
    }

    public MessageMatcher ownedCopy(String newOwner) {
        return new MessageMatcher(this).setOwner(newOwner);
    }

    public MessageMatcher setOwner(String owner) {
        this.sentTo = owner != null && !owner.isBlank() ? owner + ">>" : "";
        this.printIt = this.sentTo != null && !this.sentTo.isBlank() ? true : false;
        return this;
    }

    public MessageMatcher setPrint(boolean toPrint) {
        this.printIt = toPrint;
        return this;
    }

    private String printArgument(GameEvent argument) {
        StringBuilder sb = new StringBuilder();
        if (argument != null) {
            sb.append(argument.hashCode());
        }
        sb.append("vvvvvvvvvvvvvvvvvvvvvvvvvvv ").append(this.sentTo).append("\n");
        if (argument != null && this.sentTo != null && !this.sentTo.isBlank()) {
            sb.append(this.sentTo);
            String argumentAsString = argument.toString();
            if (argumentAsString != null) {
                sb.append(argumentAsString.replace("\n", "\n" + this.sentTo));
            } else {
                sb.append(argumentAsString);
            }
        } else {
            sb.append(argument);
        }
        sb.append("\n").append("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^").append(this.sentTo);
        return sb.toString();
    }

    @Override
    protected void failHook(GameEvent argument, String reason) {
        if (!this.printIt) {
            return;
        }
        System.out.println(String.format("%s -> %s", this.printArgument(argument),
                reason != null ? reason : "no failure reason given"));
    }

    @Override
    protected void successHook(GameEvent argument, String reason) {
        if (!this.printIt) {
            return;
        }
        System.out.println(String.format("%s -> %s", this.printArgument(argument),
                reason != null ? reason : "no success reason given"));
    }

    @Override
    public boolean matches(GameEvent argument) {
        return this.test(argument);
    }

}