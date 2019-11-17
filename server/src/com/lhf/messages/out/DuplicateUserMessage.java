package com.lhf.messages.out;

public class DuplicateUserMessage extends HelpMessage {
    public String toString() {
        StringBuilder sb = new StringBuilder("An adventurer by that name already exists! Please name your adventurer something unique.\r\n");
        sb.append(super.toString());
        return sb.toString();
    }
}
