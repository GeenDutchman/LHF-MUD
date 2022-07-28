package com.lhf.messages.out;

import com.lhf.messages.OutMessageType;

public class NoUserMessage extends OutMessage {
    public NoUserMessage() {
        super(OutMessageType.NO_USER);
    }

    public String toString() {
        return "You have neither created nor logged in as a User, so you can't do much yet.\r\nTry running 'CREATE <username> with <password>'\r\n";
    }
}
