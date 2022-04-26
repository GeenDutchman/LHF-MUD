package com.lhf.messages;

import com.lhf.server.client.ClientID;
import com.lhf.server.client.user.UserID;

public class CommandContext {
    protected ClientID clientID;
    protected UserID userID;

    public ClientID getClientID() {
        return clientID;
    }

    public void setClientID(ClientID clientID) {
        this.clientID = clientID;
    }

    public UserID getUserID() {
        return userID;
    }

    public void setUserID(UserID userID) {
        this.userID = userID;
    }

}
