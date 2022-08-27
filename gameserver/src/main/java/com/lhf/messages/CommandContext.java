package com.lhf.messages;

import com.lhf.game.battle.BattleManager;
import com.lhf.game.creature.Creature;
import com.lhf.game.map.Room;
import com.lhf.messages.out.OutMessage;
import com.lhf.server.client.ClientID;
import com.lhf.server.client.user.UserID;

public class CommandContext implements ClientMessenger {
    protected ClientMessenger client;
    protected UserID userID;
    protected Creature creature;
    protected Room room;
    protected BattleManager bManager;

    @Override
    public ClientID getClientID() {
        if (this.client != null) {
            return this.client.getClientID();
        }
        return null;
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

    public void setRoom(Room room) {
        this.room = room;
    }

    public Room getRoom() {
        return this.room;
    }

    public BattleManager getBattleManager() {
        return this.bManager;
    }

    public void setBattleManager(BattleManager battleManager) {
        this.bManager = battleManager;
    }

    @Override
    public String getStartTag() {
        return "<command_context>";
    }

    @Override
    public String getEndTag() {
        return "</command_context>";
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTag() + "context" + this.getEndTag();
    }
}
