package com.lhf.messages.out;

public class BadMessage extends OutMessage {
    public String toString() {
       return "I did not understand that message only \"say [msg]\" is supported so far";
    }
}
