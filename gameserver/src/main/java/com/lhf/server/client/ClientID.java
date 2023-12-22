package com.lhf.server.client;

import java.util.Objects;
import java.util.UUID;

public final class ClientID implements Comparable<ClientID> {
    private final UUID uuid;

    public ClientID() {
        uuid = UUID.randomUUID();
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ClientID [uuid=").append(uuid).append("]");
        return builder.toString();
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
