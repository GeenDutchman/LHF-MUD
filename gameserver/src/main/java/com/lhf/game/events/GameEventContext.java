package com.lhf.game.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.lhf.game.battle.BattleManager;
import com.lhf.game.creature.Creature;
import com.lhf.game.events.messages.ClientMessenger;
import com.lhf.game.events.messages.out.OutMessage;
import com.lhf.game.map.Dungeon;
import com.lhf.game.map.Room;
import com.lhf.server.client.ClientID;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;

public class GameEventContext implements ClientMessenger {
    protected ClientMessenger client;
    protected User user;
    protected Creature creature;
    protected Room room;
    protected BattleManager bManager;
    protected Dungeon dungeon;
    protected List<OutMessage> messages = new ArrayList<>();

    public class Reply {
        protected boolean handled;

        protected Reply(boolean isHandled) {
            this.handled = isHandled;
        }

        public List<OutMessage> getMessages() {
            if (GameEventContext.this.messages == null) {
                GameEventContext.this.messages = new ArrayList<>();
            }
            return Collections.unmodifiableList(GameEventContext.this.messages);
        }

        public boolean isHandled() {
            return handled;
        }

        public Reply resolve() {
            this.handled = true;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Reply [handled=").append(handled)
                    .append(",messageTypes=")
                    .append(this.getMessages().stream().map(outMessage -> outMessage.getOutType()).toList())
                    .append("]");
            return builder.toString();
        }

    }

    public Reply failhandle() {
        return this.new Reply(false);
    }

    public Reply handled() {
        return this.new Reply(true);
    }

    public void addMessage(OutMessage message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        if (message != null) {
            this.messages.add(message);
        }
    }

    public List<OutMessage> getMessages() {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        return Collections.unmodifiableList(this.messages);
    }

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
        if (msg != null) {
            this.addMessage(msg);
            if (this.client != null) {
                this.client.sendMsg(msg);
            }
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CommandContext [client=").append(client).append(", user=").append(user).append(", creature=")
                .append(creature).append(", room=").append(room).append(", bManager=").append(bManager)
                .append(", dungeon=").append(dungeon).append("]");
        return builder.toString();
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTag() + "context" + this.getEndTag();
    }
}
