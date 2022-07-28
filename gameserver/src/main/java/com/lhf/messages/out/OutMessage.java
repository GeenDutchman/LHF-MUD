package com.lhf.messages.out;

import com.lhf.messages.OutMessageType;

public class OutMessage {
    private OutMessageType type;

    public OutMessage(OutMessageType type) {
        this.type = type;
    }

    public OutMessageType getOutType() {
        return this.type;
    }

    protected void retype(OutMessageType type) {
        this.type = type;
    }
}
