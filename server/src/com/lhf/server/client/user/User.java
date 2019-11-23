package com.lhf.server.client.user;

import com.lhf.server.client.ClientID;
import com.lhf.server.messages.in.CreateInMessage;

public class User {
    private String username;
    private String password;
    private ClientID client;
    public User(CreateInMessage msg, ClientID client) {
        username = msg.getUsername();
        password = msg.getPassword();
        this.client = client;
    }

    public String getUsername() {
        return username;
    }
}
