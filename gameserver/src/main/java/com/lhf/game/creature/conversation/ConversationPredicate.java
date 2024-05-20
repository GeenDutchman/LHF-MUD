package com.lhf.game.creature.conversation;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;

import com.lhf.game.creature.conversation.ConversationTransformer.ConversationContext;
import com.lhf.game.creature.conversation.ConversationTransformer.ConversationContextKey;

public class ConversationPredicate implements Serializable {
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
}
