package com.lhf.game.creature.conversation;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lhf.game.creature.conversation.ConversationContext.ConversationContextKey;

public class ConversationTreeBranch implements Serializable, Comparable<ConversationTreeBranch> {
    private final ConversationPattern regex;
    private final UUID nodeID;
    private Map<String, ConversationPattern> blacklist;

    public ConversationTreeBranch(ConversationPattern regex, UUID nodeID) {
        this.regex = regex;
        this.nodeID = nodeID;
        this.blacklist = new TreeMap<>();
    }

    public ConversationTreeBranch(String regex, String example, UUID nodeID) {
        this.regex = new ConversationPattern(example, regex);
        this.nodeID = nodeID;
        this.blacklist = new TreeMap<>();
    }

    public ConversationPattern getRegex() {
        return regex;
    }

    public UUID getNodeID() {
        return nodeID;
    }

    public Map<String, ConversationPattern> getBlacklist() {
        return this.blacklist;
    }

    public ConversationPattern addRule(ConversationContextKey key, ConversationPattern pattern) {
        return this.addRule(key.name(), pattern);
    }

    public ConversationPattern addRule(String key, ConversationPattern pattern) {
        return this.blacklist.put(key, pattern);
    }

    public ConversationPattern removeRule(ConversationContextKey key) {
        return this.removeRule(key.name());
    }

    public ConversationPattern removeRule(String key) {
        return this.blacklist.remove(key);
    }

    public boolean canAccess(ConversationContext ctx) {
        for (String key : this.blacklist.keySet()) {
            if (ctx.containsKey(key)) {
                Matcher matcher = this.blacklist.get(key).matcher(ctx.get(key));
                if (matcher.find()) {
                    return false;
                }
            }
        }
        return true;
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
        if (!nodeID.equals(other.nodeID)) {
            return false;
        }
        if (this.regex.flags() != other.regex.flags()) {
            return false;
        }
        return this.regex.equals(other.regex);
    }

    @Override
    public int compareTo(ConversationTreeBranch o) {
        if (o == null) {
            throw new NullPointerException("ConversationTreeBranch is not comparable to null!");
        }
        return this.regex.toString().compareTo(o.getRegex().toString()) * -1;
    }

}
