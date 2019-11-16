package com.lhf.messages.out;

public class WrongUserMessage extends OutMessage {

    private String msg;

    public WrongUserMessage(String msg) {
        this.msg = msg;
    }

    public String toString() {
        return msg;
    }
}
