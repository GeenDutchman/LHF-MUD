package com.lhf.game.map;

import java.util.Set;
import java.util.TreeSet;

import com.lhf.messages.out.RoomEnteredOutMessage;
import com.lhf.server.client.user.User;

public class DMRoom extends Room {
    private Set<User> users;

    DMRoom(String description) {
        super(description);
        this.users = new TreeSet<>();
    }

    public boolean addUser(User user) {
        user.setSuccessor(user);
        this.sendMessageToAll(new RoomEnteredOutMessage(user));
        return this.users.add(user);
    }

    public User getUser(String username) {
        for (User user : this.users) {
            if (username.equals(user.getUsername())) {
                return user;
            }
        }
        return null;
    }

    public User removeUser(String username) {
        for (User user : this.users) {
            if (username.equals(user.getUsername())) {
                this.users.remove(user);
                return user;
            }
        }
        return null;
    }

}
