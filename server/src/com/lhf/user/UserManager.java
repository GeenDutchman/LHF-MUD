package com.lhf.user;

import com.lhf.game.Game;
import com.lhf.messages.in.CreateInMessage;
import com.lhf.server.ClientID;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public class UserManager {
    private HashMap<UserID, User> userMap;
    private HashMap<UserID, ClientID> clientMap;
    private Game game;
    public UserManager() {
        userMap = new HashMap<>();
        clientMap = new HashMap<>();
    }
    public User getUser(UserID userId) {
        return userMap.get(userId);
    }

    public Collection<UserID> getAllUserIds() {
        return userMap.keySet();
    }

    public void addUser(ClientID client, CreateInMessage msg) {
        userMap.put(new UserID(msg), new User(msg, client));
    }

    public UserID addUser(CreateInMessage msg, ClientID clientId) {
        UserID userId = new UserID(msg);
        if (!userMap.containsKey(userId)) {
            game.addNewPlayerToGame(userId, msg.getUsername());
        }
        userMap.put(userId, new User(msg, clientId));
        clientMap.put(userId, clientId);
        return userId;
    }
    public ClientID getClient(UserID id) {
        System.out.println(id);
        return clientMap.get(id);
    }

    public void removeUser(UserID id) {
        clientMap.remove(id);
    }

    public void setGame(Game g) {
        game = g;
    }
}
