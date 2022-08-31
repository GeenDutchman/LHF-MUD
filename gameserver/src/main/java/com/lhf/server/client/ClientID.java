package com.lhf.server.client;

import java.util.Objects;
import java.util.UUID;

public class ClientID implements Comparable<ClientID> {
    private UUID uuid;

    public ClientID() {
        uuid = UUID.randomUUID();
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ClientID)) {
            return false;
        }
        ClientID other = (ClientID) obj;
        return Objects.equals(uuid, other.uuid);
    }

    @Override
    public int compareTo(ClientID o) {
        return this.uuid.compareTo(o.uuid);
    }

}
