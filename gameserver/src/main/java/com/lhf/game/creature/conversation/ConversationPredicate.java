package com.lhf.game.creature.conversation;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;

import com.lhf.game.creature.conversation.ConversationTransformer.ConversationContext;
import com.lhf.game.creature.conversation.ConversationTransformer.ConversationContextKey;

public final class ConversationPredicate implements Serializable, Comparable<ConversationPredicate> {
    private final UUID uuid = UUID.randomUUID();
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

    public UUID getUuid() {
        return uuid;
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
        builder.append("ConversationPredicate [uuid=").append(uuid).append(", blacklist=").append(blacklist)
                .append("]");
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
        return this.uuid.compareTo(arg0.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ConversationPredicate))
            return false;
        ConversationPredicate other = (ConversationPredicate) obj;
        return Objects.equals(uuid, other.uuid);
    }

}
