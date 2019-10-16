package com.lhf.user;

import com.lhf.messages.in.CreateInMessage;
import com.lhf.server.ClientID;

public class User {
    String username;
    String password;
    ClientID client;
    public User(CreateInMessage msg, ClientID client) {
        username = msg.getUsername();
        password = msg.getPassword();
        this.client = client;
    }

    public String getUsername() {
        return username;
    }
}
