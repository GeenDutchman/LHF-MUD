package com.lhf.messages;

import com.lhf.game.creature.Creature;
import com.lhf.messages.out.OutMessage;
import com.lhf.server.client.ClientID;
import com.lhf.server.client.user.UserID;

public class CommandContext implements ClientMessenger {
    protected ClientMessenger client;
    protected UserID userID;
    protected Creature creature;

    @Override
    public ClientID getClientID() {
        return this.client.getClientID();
    }

    public ClientMessenger getClientMessenger() {
        return this.client;
    }

    public Creature getCreature() {
        return creature;
    }

    public void setCreature(Creature creature) {
        this.creature = creature;
    }

    @Override
    public void sendMsg(OutMessage msg) {
        if (this.client != null) {
            this.client.sendMsg(msg);
        }
    }

    public void setClient(ClientMessenger client) {
        this.client = client;
    }

    public UserID getUserID() {
        return userID;
    }

    public void setUserID(UserID userID) {
        this.userID = userID;
    }
}
