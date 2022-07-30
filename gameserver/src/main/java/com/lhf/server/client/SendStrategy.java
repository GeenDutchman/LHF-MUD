package com.lhf.server.client;

import com.lhf.messages.out.OutMessage;

public interface SendStrategy {
    public void send(OutMessage toSend);
}
