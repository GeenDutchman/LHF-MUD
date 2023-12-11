package com.lhf.server.client.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.lhf.messages.ClientMessenger;
import com.lhf.messages.in.CreateInMessage;
import com.lhf.server.client.ClientID;

public class UserManager {
    private HashMap<UserID, User> userMap;
    private HashMap<UserID, ClientID> clientMap;

    public UserManager() {
        userMap = new HashMap<>();
        clientMap = new HashMap<>();
    }

    public User getUser(UserID userId) {
        return userMap.get(userId);
    }

    public List<String> getAllUsernames() {
        ArrayList<User> users = new ArrayList<>(userMap.values());
        ArrayList<String> usernames = new ArrayList<>();
        for (User user : users) {
            usernames.add(user.getUsername());
        }
        return usernames;
    }

    public List<String> getForbiddenUsernames() {
        List<String> usernames = this.getAllUsernames();
        usernames.add("Someone");
        usernames.add("someone");
        usernames.add("You");
        usernames.add("you");
        return usernames;
    }

    public Collection<UserID> getAllUserIds() {
        return userMap.keySet();
    }

    public User addUser(CreateInMessage msg, ClientMessenger client) {
        User user = new User(msg, client);
        if (!userMap.containsKey(user.getUserID())) {
            userMap.put(user.getUserID(), user);
            clientMap.put(user.getUserID(), client.getClientID());
            return user;
        }
        return null;
    }

    public ClientID getClient(UserID id) {
        System.out.println(id);
        return clientMap.get(id);
    }

    public void removeUser(UserID id) {
        // should remove from both
        clientMap.remove(id);
        userMap.remove(id);
    }

}
