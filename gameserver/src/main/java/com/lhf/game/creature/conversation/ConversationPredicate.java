package com.lhf.game.creature.conversation;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;

import com.lhf.game.creature.conversation.ConversationTransformer.ConversationContext;
import com.lhf.game.creature.conversation.ConversationTransformer.ConversationContextKey;

public final class ConversationPredicate implements Serializable, Comparable<ConversationPredicate> {
    private final SortedMap<String, ConversationPattern> blacklist;

    public static class Builder implements Serializable {
        private SortedMap<String, ConversationPattern> blacklist = new TreeMap<>();

        public Builder setRules(Map<String, ConversationPattern> newRules) {
            this.blacklist.clear();
            return this.addRules(newRules);
        }

        public Builder addRules(Map<String, ConversationPattern> newRules) {
            if (newRules != null) {
                this.blacklist.putAll(newRules);
            }
            return this;
        }

        public Builder addRule(ConversationContextKey key, ConversationPattern pattern) {
            this.addRule(key.name(), pattern);
            return this;
        }

        public Builder addRule(String key, ConversationPattern pattern) {
            this.blacklist.put(key, pattern);
            return this;
        }

        public Builder removeRule(ConversationContextKey key) {
            this.removeRule(key.name());
            return this;
        }

        public Builder removeRule(String key) {
            this.blacklist.remove(key);
            return this;
        }

        public SortedMap<String, ConversationPattern> getBlacklist() {
            if (blacklist == null) {
                this.blacklist = new TreeMap<>();
            }
            return this.blacklist;
        }

        public ConversationPredicate build() {
            return new ConversationPredicate(this.getBlacklist());
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Builder [blacklist=").append(blacklist).append("]");
            return builder.toString();
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static ConversationPredicate copyFrom(ConversationPredicate other) {
        return new ConversationPredicate(other != null ? other.getBlacklist() : null);
    }

    public ConversationPredicate(Map<String, ConversationPattern> blacklist) {
        TreeMap<String, ConversationPattern> map = new TreeMap<>();
        if (blacklist != null) {
            map.putAll(blacklist);
        }
        this.blacklist = Collections.unmodifiableSortedMap(map);
    }

    public Map<String, ConversationPattern> getBlacklist() {
        return this.blacklist != null ? this.blacklist : Map.of();
    }

    public int size() {
        return this.blacklist.size();
    }

    public boolean canAccess(ConversationContext ctx) {
        if (this.blacklist == null) {
            return true;
        }
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
        StringBuilder builder2 = new StringBuilder();
        builder2.append("ConversationPredicate [blacklist=").append(blacklist).append("]");
        return builder2.toString();
    }

    @Override
    public int compareTo(ConversationPredicate other) {
        if (other == null) {
            throw new NullPointerException();
        }
        if (this.equals(other)) {
            return 0;
        }
        if (this.blacklist != null && other.blacklist == null) {
            return -1;
        } else if (this.blacklist == null && other.blacklist != null) {
            return 1;
        } else if (this.blacklist == null && other.blacklist == null) {
            return 0;
        }
        int comparison = other.blacklist.size() - this.blacklist.size(); // larger first
        if (comparison != 0) {
            return comparison;
        }
        final String myString = this.blacklist.toString();
        final String otherString = other.blacklist.toString();
        comparison = otherString.length() - myString.length(); // longest first
        if (comparison != 0) {
            return comparison;
        }
        return this.blacklist.toString().compareTo(other.blacklist.toString()); // now lexicographically
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
