package com.lhf.server.messages.out;

public class WrongUserMessage extends OutMessage {

    private String msg;

    public WrongUserMessage(String username) {
        this.msg = "There's no <player>" +
                username +
                "</player> in this dungeon. " +
                "Message not sent.";
    }

    public String toString() {
        return msg;
    }
}
