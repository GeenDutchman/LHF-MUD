package com.lhf.game.creature.conversation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

public class ConversationContext implements Map<String, String> {
    public enum ConversationContextKey {
        TALKER_NAME, TALKER_TAGGED_NAME, LISTENER_NAME, LISTENER_TAGGED_NAME;
    }

    private List<UUID> trail;
    private Map<String, String> contextBag;

    public ConversationContext() {
        this.trail = new ArrayList<>();
        this.contextBag = new TreeMap<>();
    }

    public boolean addTrail(UUID nodeID) {
        if (nodeID.equals(this.getTrailEnd())) {
            return false;
        }
        return this.trail.add(nodeID);
    }

    public UUID getTrailEnd() {
        if (this.trail.size() <= 0) {
            return null;
        }
        return this.trail.get(this.trail.size() - 1);
    }

    @Override
    public void clear() {
        this.contextBag.clear();
    }

    public boolean containsKey(ConversationContextKey key) {
        return this.containsKey(key.name());
    }

    @Override
    public boolean containsKey(Object key) {
        return this.contextBag.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.contextBag.containsValue(value);
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return this.contextBag.entrySet();
    }

    public String get(ConversationContextKey key) {
        return this.get(key.name());
    }

    @Override
    public String get(Object key) {
        return this.contextBag.get(key);
    }

    @Override
    public boolean isEmpty() {
        return this.contextBag.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return this.contextBag.keySet();
    }

    public String put(ConversationContextKey arg0, String arg1) {
        return this.put(arg0.name(), arg1);
    }

    @Override
    public String put(String arg0, String arg1) {
        return this.contextBag.put(arg0, arg1);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        this.contextBag.putAll(m);
    }

    public String remove(ConversationContextKey key) {
        return this.remove(key.name());
    }

    @Override
    public String remove(Object key) {
        return this.contextBag.remove(key);
    }

    @Override
    public int size() {
        return this.contextBag.size();
    }

    @Override
    public Collection<String> values() {
        return this.contextBag.values();
    }

}
