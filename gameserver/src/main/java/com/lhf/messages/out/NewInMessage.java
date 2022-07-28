package com.lhf.messages.out;

import com.lhf.messages.OutMessageType;

public class NewInMessage extends OutMessage {
    public NewInMessage() {
        super(OutMessageType.NEW_IN);
    }

    @Override
    public String toString() {
        return "New User in Server\r\n";
    }
}
