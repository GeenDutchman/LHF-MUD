package com.lhf.server.client;

import java.util.Optional;

public class ClientID {
    private Optional<Integer> index;
    private static int total_count;
    public ClientID() {
        index = Optional.of(total_count++);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClientID) {
            ClientID clientObj = (ClientID) obj;
            return clientObj.getIndex() == index;
        }
        return false;
    }

    private Optional<Integer> getIndex() {
        return index;
    }
}
