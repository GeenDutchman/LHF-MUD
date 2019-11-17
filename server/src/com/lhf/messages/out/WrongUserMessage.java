package com.lhf.messages.out;

public class WrongUserMessage extends OutMessage {

    private String msg;

    public WrongUserMessage(String username) {
        StringBuilder sb = new StringBuilder();
        sb.append("There's no <player>");
        sb.append(username);
        sb.append("</player> in this dungeon. ");
        sb.append("Message not sent.");
        this.msg = sb.toString();
    }

    public String toString() {
        return msg;
    }
}
