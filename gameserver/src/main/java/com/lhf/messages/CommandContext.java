package com.lhf.messages;

import com.lhf.game.battle.BattleManager;
import com.lhf.game.creature.Creature;
import com.lhf.game.map.Dungeon;
import com.lhf.game.map.Room;
import com.lhf.messages.out.OutMessage;
import com.lhf.server.client.ClientID;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;

public class CommandContext implements ClientMessenger {
    protected ClientMessenger client;
    protected User user;
    protected Creature creature;
    protected Room room;
    protected BattleManager bManager;
    protected Dungeon dungeon;

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

    public User getUser() {
        return user;
    }

    public UserID getUserID() {
        if (user != null) {
            return user.getUserID();
        }
        return null;
    }

    public void setUser(User user) {
        this.user = user;
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

    public Dungeon getDungeon() {
        return dungeon;
    }

    public void setDungeon(Dungeon dungeon) {
        this.dungeon = dungeon;
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
