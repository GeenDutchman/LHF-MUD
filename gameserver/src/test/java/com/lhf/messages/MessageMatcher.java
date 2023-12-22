package com.lhf.messages;

import java.util.ArrayList;
import java.util.List;

import org.mockito.ArgumentMatcher;

import com.lhf.messages.out.GameEvent;

public class MessageMatcher implements ArgumentMatcher<GameEvent> {

    protected GameEventType type;
    protected List<String> contained;
    protected List<String> notContained;
    protected boolean printIt = false;
    protected String sentTo = "";

    public MessageMatcher(GameEventType type, List<String> containedWords, List<String> notContainedWords) {
        this.type = type;
        this.contained = containedWords;
        this.notContained = notContainedWords;
    }

    public MessageMatcher(GameEventType type, String contained) {
        this.type = type;
        this.contained = List.of(contained);
        this.notContained = null;
    }

    public MessageMatcher(GameEventType type) {
        this.type = type;
        this.contained = null;
        this.notContained = null;
    }

    public MessageMatcher(String contained) {
        this.contained = List.of(contained);
        this.notContained = null;
        this.type = null;
    }

    public MessageMatcher ownedCopy(String newOwner) {
        return new MessageMatcher(this.type, this.contained != null ? new ArrayList<>(this.contained) : null,
                this.notContained != null ? new ArrayList<>(this.notContained) : null).setOwner(newOwner);
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

    private String printArgument(String argumentAsString) {
        StringBuilder sb = new StringBuilder("vvvvvvvvvvvvvvvvvvvvvvvvvvv " + this.sentTo + "\n");
        if (this.sentTo != null && !this.sentTo.isBlank()) {
            sb.append(this.sentTo);
            if (argumentAsString != null) {
                sb.append(argumentAsString.replace("\n", "\n" + this.sentTo));
            } else {
                sb.append(argumentAsString);
            }
        } else {
            sb.append(argumentAsString);
        }
        sb.append("\n").append("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^").append(this.sentTo);
        return sb.toString();
    }

    @Override
    public boolean matches(GameEvent argument) {
        if (argument == null) {
            if (this.printIt) {
                System.out.println(this.printArgument(null) + "null, no match");
            }
            return false;
        }
        String argumentAsString = argument.toString();
        StringBuilder sb = new StringBuilder().append(argument.hashCode()).append(this.printArgument(argumentAsString));

        if (this.type != null && this.type != argument.getEventType()) {
            if (this.printIt) {
                sb.append("expected type ").append(this.type).append(" got type ").append(argument.getEventType())
                        .append(",no match");
                System.out.println(sb.toString());
            }
            return false;
        }

        if (this.contained != null) {
            for (String words : this.contained) {
                if (!argumentAsString.contains(words)) {
                    if (this.printIt) {
                        sb.append("expected words \"").append(words).append("\" not found, no match");
                        System.out.println(sb.toString());
                    }
                    return false;
                }
            }
        }

        if (this.notContained != null) {
            for (String words : this.notContained) {
                if (argumentAsString.contains(words)) {
                    if (this.printIt) {
                        sb.append("not expected words \"").append(words).append("\", but found, no match");
                        System.out.println(sb.toString());
                    }
                    return false;
                }
            }
        }

        if (this.printIt) {
            sb.append("matched");
            System.out.println(sb.toString());
        }
        return true;
    }

}