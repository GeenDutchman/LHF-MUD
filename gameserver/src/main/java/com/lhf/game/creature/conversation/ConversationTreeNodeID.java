package com.lhf.game.creature.conversation;

import java.util.Objects;

import com.lhf.server.interfaces.NotNull;

public class ConversationTreeNodeID implements Comparable<ConversationTreeNodeID> {

    private final String id;

    public ConversationTreeNodeID(@NotNull String nodeID) {
        this.id = nodeID;
    }

    public String getNodeID() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.id;
    }

    @Override
    public int compareTo(ConversationTreeNodeID o) {
        return this.id.compareTo(o.getNodeID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ConversationTreeNodeID)) {
            return false;
        }
        ConversationTreeNodeID other = (ConversationTreeNodeID) obj;
        return Objects.equals(id, other.id);
    }

}