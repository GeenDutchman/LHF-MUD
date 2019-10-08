package com.lhf.messages;

public class BadMessage extends UserMessage {
    public String toString() {
       return "I did not understand that message only \"say [msg]\" is supported so far";
    }
}
