package com.lhf.messages.out;

public class BadMessage extends HelpMessage {
    public String toString() {
        StringBuilder sb = new StringBuilder("I did not understand that command!\n\r");
        sb.append(super.toString());
        return sb.toString();
    }
}
