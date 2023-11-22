package com.lhf.game.events.messages;

import java.util.ArrayList;
import java.util.List;

import org.mockito.ArgumentMatcher;

public class CommandMatcher implements ArgumentMatcher<Command> {
    protected CommandMessage cmd;
    protected List<String> contained;
    protected List<String> notContained;
    protected boolean printIt = false;
    protected String sentBy = "";

    public CommandMatcher(CommandMessage cmd, List<String> contained, List<String> notContained) {
        this.cmd = cmd;
        this.contained = contained;
        this.notContained = notContained;
    }

    public CommandMatcher(CommandMessage cmd, String contained) {
        this.cmd = cmd;
        this.contained = List.of(contained);
        this.notContained = null;
    }

    public CommandMatcher(CommandMessage cmd) {
        this.cmd = cmd;
        this.contained = null;
        this.notContained = null;
    }

    public CommandMatcher ownedCopy(String newOwner) {
        return new CommandMatcher(this.cmd, this.contained != null ? new ArrayList<>(this.contained) : null,
                this.notContained != null ? new ArrayList<>(this.notContained) : null).setOwner(newOwner);
    }

    public CommandMatcher setOwner(String owner) {
        this.sentBy = owner != null && !owner.isBlank() ? owner + ">>" : "";
        this.printIt = this.sentBy != null && !this.sentBy.isBlank() ? true : false;
        return this;
    }

    public CommandMatcher setPrint(boolean toPrint) {
        this.printIt = toPrint;
        return this;
    }

    private String printArgument(String argumentAsString) {
        StringBuilder sb = new StringBuilder("vvvvvvvvvvvvvvvvvvvvvvvvvvv " + this.sentBy + "\n");
        if (this.sentBy != null && !this.sentBy.isBlank()) {
            sb.append(this.sentBy);
            if (argumentAsString != null) {
                sb.append(argumentAsString.replace("\n", "\n" + this.sentBy));
            } else {
                sb.append(argumentAsString);
            }
        } else {
            sb.append(argumentAsString);
        }
        sb.append("\n").append("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^").append(this.sentBy);
        return sb.toString();
    }

    @Override
    public boolean matches(Command argument) {
        if (argument == null) {
            if (this.printIt) {
                System.out.println(this.printArgument(null) + "null, no match");
            }
            return false;
        }
        String argumentAsString = argument.getWhole();
        StringBuilder sb = new StringBuilder().append(argument.hashCode()).append(this.printArgument(argumentAsString));

        if (this.cmd != null && this.cmd != argument.getType()) {
            if (this.printIt) {
                sb.append("expected type ").append(this.cmd).append(" got type ").append(argument.getType())
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
