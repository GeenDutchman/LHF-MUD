package com.lhf.messages.in;

public class ExamineMessage extends InMessage {

    private String thing;

    public ExamineMessage(String payload) {
        thing = payload;
    }

    public String getThing() {
        return thing;
    }
}
