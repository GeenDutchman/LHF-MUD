package com.lhf.messages;

import java.util.List;

import org.mockito.ArgumentMatcher;

import com.lhf.messages.out.OutMessage;

public class MessageMatcher implements ArgumentMatcher<OutMessage> {

    protected OutMessageType type;
    protected List<String> contained;
    protected List<String> notContained;
    protected boolean printIt = false;
    protected String sentTo = "";

    public MessageMatcher(OutMessageType type, List<String> containedWords, List<String> notContainedWords) {
        this.type = type;
        this.contained = containedWords;
        this.notContained = notContainedWords;
    }

    public MessageMatcher(OutMessageType type, String contained) {
        this.type = type;
        this.contained = List.of(contained);
        this.notContained = null;
    }

    public MessageMatcher(OutMessageType type) {
        this.type = type;
        this.contained = null;
        this.notContained = null;
    }

    public MessageMatcher(String contained) {
        this.contained = List.of(contained);
        this.notContained = null;
        this.type = null;
    }

    public MessageMatcher setPrint(boolean toPrint, String sentTo) {
        this.printIt = toPrint;
        this.sentTo = sentTo != null && !sentTo.isBlank() ? "sent to:" + sentTo + ":\n" : "";
        return this;
    }

    @Override
    public boolean matches(OutMessage argument) {
        if (argument == null) {
            if (this.printIt) {
                System.out.println(this.sentTo + "null, no match");
            }
            return false;
        }
        String argumentAsString = argument.toString();

        if (this.type != null && this.type != argument.getOutType()) {
            if (this.printIt) {
                System.out.printf("%s%s\n>expected type %s got type %s, no match\n", this.sentTo, argumentAsString,
                        this.type.toString(),
                        argument.getOutType() != null ? argument.getOutType().toString() : "null");
            }
            return false;
        }

        if (this.contained != null) {
            for (String words : this.contained) {
                if (!argumentAsString.contains(words)) {
                    if (this.printIt) {
                        System.out.printf("%s%s\n>expected words \"%s\", not found, no match\n", this.sentTo,
                                argumentAsString, words);
                    }
                    return false;
                }
            }
        }

        if (this.notContained != null) {
            for (String words : this.notContained) {
                if (argumentAsString.contains(words)) {
                    if (this.printIt) {
                        System.out.printf("%s%s\n>did not expect words \"%s\", but found, no match", this.sentTo,
                                argumentAsString, words);
                    }
                    return false;
                }
            }
        }

        if (this.printIt) {
            System.out.printf("%s%s\n>matched", this.sentTo, argumentAsString);
        }
        return true;
    }

}