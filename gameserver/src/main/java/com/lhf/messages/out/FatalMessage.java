package com.lhf.messages.out;

import com.lhf.messages.OutMessageType;

public class FatalMessage extends OutMessage {
    public FatalMessage() {
        super(OutMessageType.FATAL);
    }

    @Override
    public String toString() {
        return "You made a fatal mistake";
    }
}
