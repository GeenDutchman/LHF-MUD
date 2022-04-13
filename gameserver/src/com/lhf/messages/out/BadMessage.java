package com.lhf.messages.out;

public class BadMessage extends HelpMessage {
    public String toString() {
        return "I did not understand that command!\r\n" + super.toString();
    }
}
