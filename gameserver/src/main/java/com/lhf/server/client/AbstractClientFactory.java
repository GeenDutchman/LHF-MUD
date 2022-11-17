package com.lhf.server.client;

import java.util.Map;
import java.util.TreeMap;

public class AbstractClientFactory {
    protected Map<String, ClientFactory> factories = new TreeMap<>();

    public AbstractClientFactory register(String key, ClientFactory factory) {
        this.factories.put(key, factory);
        return this;
    }

    public ClientFactory getFactory(String key) {
        return this.factories.get(key);
    }
}
