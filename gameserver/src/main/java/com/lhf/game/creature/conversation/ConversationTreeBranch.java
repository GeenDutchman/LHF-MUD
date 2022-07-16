package com.lhf.game.creature.conversation;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public class ConversationTreeBranch implements Serializable, Comparable<ConversationTreeBranch> {
    private final Pattern regex;
    private final UUID nodeID;

    public ConversationTreeBranch(Pattern regex, UUID nodeID) {
        this.regex = regex;
        this.nodeID = nodeID;
    }

    public ConversationTreeBranch(String regex, UUID nodeID) {
        this.regex = Pattern.compile(regex);
        this.nodeID = nodeID;
    }

    public Pattern getRegex() {
        return regex;
    }

    public UUID getNodeID() {
        return nodeID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(regex, nodeID);
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
        return Objects.equals(regex, other.regex) && Objects.equals(nodeID, other.nodeID);
    }

    @Override
    public int compareTo(ConversationTreeBranch o) {
        if (o == null) {
            throw new NullPointerException("ConversationTreeBranch is not comparable to null!");
        }
        return this.regex.toString().compareTo(o.getRegex().toString()) * -1;
    }

}
