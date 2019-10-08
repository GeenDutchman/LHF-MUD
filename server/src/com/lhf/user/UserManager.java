package com.lhf.user;

import com.lhf.server.ClientHandle;
import com.sun.security.ntlm.Client;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

public class UserManager {
    private HashMap<UserID, User> userMap;
    private HashMap<UserID, ClientHandle> clientMap;
    public UserManager() {
        userMap = new HashMap<>();
        clientMap = new HashMap<>();
    }
    public User getUser(UserID userId) {
        return userMap.get(userId);
    }

    public ClientHandle getConnection(UserID userId) {
        return clientMap.get(userId);
    }

    public void addUser(UserID userId) {
        userMap.put(userId, new User());
    }
    public void addUser(UserID userId, ClientHandle connection) {
        addUser(userId);
        clientMap.put(userId, connection);
    }
    public Collection<ClientHandle> getHandles() {
        return clientMap.values();
    }

    public Collection<ClientHandle> getAllHandlesExcept(UserID id) {
        HashMap<UserID, ClientHandle> clone = (HashMap<UserID, ClientHandle>) clientMap.clone();
        clone.remove(id);
        return clone.values();
    }

    public void removeUser(UserID id) throws IOException {
        clientMap.get(id).disconnect();
        userMap.remove(id);
        clientMap.remove(id);
    }
}
