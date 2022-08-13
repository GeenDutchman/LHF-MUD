package com.lhf.messages;

import java.util.HashMap;
import java.util.Map;

import com.lhf.messages.out.OutMessage;

public class OutMessagePair {
    public OutMessage toEveryone;
    public Map<ClientMessenger, OutMessage> directed;

    public OutMessagePair(OutMessage toEveryone) {
        this.toEveryone = toEveryone;
        this.directed = new HashMap<>();
    }

    public OutMessagePair(OutMessage toEveryone, Map<ClientMessenger, OutMessage> directed) {
        this.toEveryone = toEveryone;
        this.directed = directed;
    }

    public OutMessagePair addDirected(ClientMessenger messenger, OutMessage message) {
        this.directed.put(messenger, message);
        return this;
    }
}
