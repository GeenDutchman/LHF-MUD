package com.lhf.messages.out;

public class DuplicateUserMessage extends WelcomeMessage {
    public String toString() {
        return "An adventurer by that name already exists! Please name your adventurer something unique.\r\n"
                + super.toString();
    }
}
