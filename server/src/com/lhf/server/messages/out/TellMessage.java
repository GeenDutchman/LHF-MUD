package com.lhf.server.messages.out;

import com.lhf.server.client.user.UserID;

public class TellMessage extends OutMessage {
    private UserID from;
    private String message;
    public TellMessage(UserID from, String msg) {
        this.from = from;
        message = msg;
    }
    public String toString() {
        return "<tell>" + from.getUsername() + ": " + message + "</tell>";
    }
}
