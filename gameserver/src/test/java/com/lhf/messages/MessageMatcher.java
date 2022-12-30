package com.lhf.messages;

import java.util.List;

import org.mockito.ArgumentMatcher;

import com.lhf.messages.OutMessageType;
import com.lhf.messages.out.OutMessage;

public class MessageMatcher implements ArgumentMatcher<OutMessage> {

    protected OutMessageType type;
    protected List<String> contained;
    protected List<String> notContained;

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

    @Override
    public boolean matches(OutMessage argument) {
        if (argument == null) {
            return false;
        }
        if (this.type != null && this.type != argument.getOutType()) {
            return false;
        }
        String argumentAsString = argument.toString();

        if (this.contained != null) {
            for (String words : this.contained) {
                if (!argumentAsString.contains(words)) {
                    return false;
                }
            }
        }

        if (this.notContained != null) {
            for (String words : this.notContained) {
                if (argumentAsString.contains(words)) {
                    return false;
                }
            }
        }
        return true;
    }

}