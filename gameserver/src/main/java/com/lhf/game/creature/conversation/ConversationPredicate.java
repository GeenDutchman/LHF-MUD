package com.lhf.game.creature.conversation;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;

import com.lhf.game.creature.conversation.ConversationTransformer.ConversationContext;
import com.lhf.game.creature.conversation.ConversationTransformer.ConversationContextKey;

public final class ConversationPredicate implements Serializable, Comparable<ConversationPredicate> {
    private TreeMap<String, ConversationPattern> blacklist = new TreeMap<>();

    public static ConversationPredicate copyFrom(ConversationPredicate other) {
        ConversationPredicate value = new ConversationPredicate();
        if (other != null) {
            value.blacklist.putAll(other.blacklist);
        }
        return value;
    }

    public Map<String, ConversationPattern> getBlacklist() {
        if (this.blacklist == null) {
            this.blacklist = new TreeMap<>();
        }
        return this.blacklist;
    }

    public int size() {
        return this.blacklist.size();
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
                Matcher matcher = this.blacklist.get(key)
                        .matcher(ctx.getOrDefault(key, ConversationTransformer.ofString("")).getOutputBody());
                if (matcher.find()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConversationPredicate [blacklist=").append(blacklist).append("]");
        return builder.toString();
    }

    @Override
    public int compareTo(ConversationPredicate arg0) {
        if (arg0 == null) {
            throw new NullPointerException();
        }
        if (this.equals(arg0)) {
            return 0;
        }
        int compare = this.blacklist.size() - arg0.blacklist.size();
        if (compare != 0) {
            return compare;
        }
        Iterator<Entry<String, ConversationPattern>> myiter = this.blacklist.entrySet().iterator();
        Iterator<Entry<String, ConversationPattern>> otheriter = arg0.blacklist.entrySet().iterator();
        while (myiter.hasNext() && otheriter.hasNext()) {
            Entry<String, ConversationPattern> myEntry = myiter.next();
            Entry<String, ConversationPattern> otherEntry = otheriter.next();
            compare = myEntry.getKey().compareTo(otherEntry.getKey());
            if (compare != 0) {
                return compare;
            }
            compare = myEntry.getValue().compareTo(otherEntry.getValue());
            if (compare != 0) {
                return compare;
            }
        }
        return 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(blacklist);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ConversationPredicate))
            return false;
        ConversationPredicate other = (ConversationPredicate) obj;
        return Objects.equals(blacklist, other.blacklist);
    }

}
