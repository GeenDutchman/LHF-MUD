package com.lhf.messages.out;

import com.lhf.messages.OutMessageType;

public class DuplicateUserMessage extends WelcomeMessage {

    public DuplicateUserMessage() {
        this.retype(OutMessageType.DUPLICATE_USER);
    }

    public String toString() {
        return "An adventurer by that name already exists! Please name your adventurer something unique.\r\n"
                + super.toString();
    }
}
