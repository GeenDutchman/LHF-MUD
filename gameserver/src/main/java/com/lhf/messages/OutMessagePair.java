package com.lhf.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lhf.messages.out.OutMessage;

public class OutMessagePair {
    public OutMessage toEveryone;
    public Map<ClientMessenger, List<OutMessage>> directed;

    public OutMessagePair(OutMessage toEveryone) {
        this.toEveryone = toEveryone;
        this.directed = new HashMap<>();
    }

    public OutMessagePair(OutMessage toEveryone, Map<ClientMessenger, List<OutMessage>> directed) {
        this.toEveryone = toEveryone;
        this.directed = directed;
    }

    public OutMessagePair addDirected(ClientMessenger messenger, OutMessage message) {
        if (this.directed.containsKey(messenger)) {
            this.directed.put(messenger, new ArrayList<>());
        }
        this.directed.get(messenger).add(message);
        return this;
    }
}
