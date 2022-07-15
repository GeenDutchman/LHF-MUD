package com.lhf.game.creature.conversation;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public class ConversationTreeBranch implements Serializable, Comparable<ConversationTreeBranch> {
    private final Pattern keyword;
    private final UUID nodeID;

    public ConversationTreeBranch(Pattern keyword, UUID nodeID) {
        this.keyword = keyword;
        this.nodeID = nodeID;
    }

    public ConversationTreeBranch(String regex, UUID nodeID) {
        this.keyword = Pattern.compile(regex);
        this.nodeID = nodeID;
    }

    public Pattern getKeyword() {
        return keyword;
    }

    public UUID getNodeID() {
        return nodeID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyword, nodeID);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ConversationTreeBranch)) {
            return false;
        }
        ConversationTreeBranch other = (ConversationTreeBranch) obj;
        return Objects.equals(keyword, other.keyword) && Objects.equals(nodeID, other.nodeID);
    }

    @Override
    public int compareTo(ConversationTreeBranch o) {
        if (o == null) {
            throw new NullPointerException("ConversationTreeBranch is not comparable to null!");
        }
        return this.keyword.toString().compareTo(o.getKeyword().toString());
    }

}
