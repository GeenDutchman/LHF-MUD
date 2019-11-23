package com.lhf.server.messages.in;

public class ExamineMessage extends InMessage {

    private String thing;

    ExamineMessage(String payload) {
        thing = payload;
    }

    public String getThing() {
        return thing;
    }
}
