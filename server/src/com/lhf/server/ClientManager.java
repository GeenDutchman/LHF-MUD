package com.lhf.server;

import com.lhf.user.UserID;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;

public class ClientManager {
    private HashMap<ClientID, ClientHandle> clientMap;
    private HashMap<ClientID, UserID> userMap;
    private Logger logger;

    public ClientManager() {
        clientMap = new HashMap<>();
        userMap = new HashMap<>();
        logger = Logger.getLogger(this.getClass().getName());
    }

    public ClientHandle getConnection(ClientID id) {
        return clientMap.get(id);
    }

    public void addClient(@NotNull ClientID clientID, ClientHandle connection) {
        clientMap.put(clientID, connection);
    }
    public void removeClient(ClientID id) throws IOException {
        clientMap.get(id).disconnect();
        clientMap.remove(id);
    }

    public void addUserForClient(@NotNull ClientID clientID, @NotNull UserID userId){
        logger.finer("Pairing client " + clientID + " with user " + userId);
        userMap.put(clientID, userId);
    }

    public Collection<ClientHandle> getHandles() {
        return clientMap.values();
    }

    public Optional<UserID> getUserForClient(ClientID id) {
        logger.finer("Checking if client " + id + " is here.");
        Optional<UserID> result = Optional.ofNullable((userMap.get(id)));
        logger.finer("isHere:" + result.isPresent());
        return result;
    }
}