package com.lhf.game.map;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.lhf.game.creature.Player;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.ShoutMessage;
import com.lhf.messages.out.GameMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.SpawnMessage;
import com.lhf.server.client.user.UserID;

public class Dungeon implements MessageHandler {
    private Room startingRoom = null;
    private Set<Room> rooms;
    private MessageHandler successor;
    private Map<CommandMessage, String> commands;

    Dungeon(MessageHandler successor) {
        rooms = new HashSet<>();
        this.successor = successor;
        this.commands = this.buildCommands();
    }

    private Map<CommandMessage, String> buildCommands() {
        StringBuilder sb = new StringBuilder();
        Map<CommandMessage, String> cmds = new HashMap<>();
        sb.append("\"shout [message]\" ").append("Tells everyone in the dungeon your message!");
        cmds.put(CommandMessage.SHOUT, sb.toString());
        return cmds;
    }

    public boolean addNewPlayer(Player p) {
        this.sendMessageToAllExcept(new SpawnMessage(p.getColorTaggedName()), p.getName());
        p.setSuccessor(this);
        return startingRoom.addPlayer(p);
    }

    public boolean removePlayer(UserID id) {
        Room room = getPlayerRoom(id);
        if (room == null) {
            return true;
        }
        return room.removePlayer(id);
    }

    public boolean removePlayer(Player p) {
        return this.removePlayer(p.getId());
    }

    void reincarnate(Player p) {
        Player p2 = new Player(p.getUser());
        addNewPlayer(p2);
        p.sendMsg(new GameMessage(
                "*******************************X_X*********************************************\nYou have died. Out of mercy you have been reborn back where you began."));
        p2.sendMsg(new GameMessage(startingRoom.toString()));
    }

    void setStartingRoom(Room r) {
        startingRoom = r;
    }

    boolean addRoom(Room r) {
        r.setDungeon(this);
        r.setSuccessor(this);
        return rooms.add(r);
    }

    public Room getPlayerRoom(UserID id) {
        for (Room r : rooms) {
            Player p = r.getPlayerInRoom(id);
            if (p != null) {
                return r;
            }
        }
        return null;
    }

    public Player getPlayerById(UserID id) {
        for (Room r : rooms) {
            Player p = r.getPlayerInRoom(id);
            if (p != null) {
                return p;
            }
        }
        return null;
    }

    public Set<UserID> getPlayersInRoom(UserID id) {
        Set<Player> players = Objects.requireNonNull(getPlayerRoom(id)).getAllPlayersInRoom();
        Set<UserID> ids = new HashSet<>();
        for (Player p : players) {
            ids.add(p.getId());
        }
        return ids;
    }

    public void sendMessageToAllInRoom(Room room, OutMessage msg) {
        if (room == null) {
            this.startingRoom.sendMessageToAll(msg);
            return;
        }
        room.sendMessageToAll(msg);
    }

    public void sendMessageToAllInRoomExcept(Room room, OutMessage msg, String... exactNames) {
        if (room == null) {
            this.startingRoom.sendMessageToAllExcept(msg, exactNames);
            return;
        }
        room.sendMessageToAllExcept(msg, exactNames);
    }

    public void sendMessageToAll(OutMessage msg) {
        if (!this.rooms.contains(this.startingRoom)) {
            this.sendMessageToAllInRoom(this.startingRoom, msg);
        }
        for (Room room : this.rooms) {
            this.sendMessageToAllInRoom(room, msg);
        }
    }

    public void sendMessageToAllExcept(OutMessage msg, String... exactNames) {
        if (!this.rooms.contains(this.startingRoom)) {
            this.sendMessageToAllInRoomExcept(this.startingRoom, msg, exactNames);
        }
        for (Room room : this.rooms) {
            this.sendMessageToAllInRoomExcept(room, msg, exactNames);
        }
    }

    private Boolean handleShout(CommandContext ctx, Command cmd) {
        if (cmd.getType() == CommandMessage.SHOUT) {
            ShoutMessage shoutMessage = (ShoutMessage) cmd;
            for (Room room : this.rooms) {
                for (Player p : room.getAllPlayersInRoom()) {
                    p.sendMsg(new GameMessage(
                            ctx.getCreature().getColorTaggedName() + " SHOUTS: " + shoutMessage.getMessage()));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void setSuccessor(MessageHandler successor) {
        this.successor = successor;
    }

    @Override
    public MessageHandler getSuccessor() {
        return this.successor;
    }

    @Override
    public Map<CommandMessage, String> getCommands() {
        return this.commands;
    }

    @Override
    public Boolean handleMessage(CommandContext ctx, Command msg) {
        Boolean performed = false;
        if (msg.getType() == CommandMessage.SHOUT) {
            performed = this.handleShout(ctx, msg);
            if (performed) {
                return performed;
            }
        }
        return MessageHandler.super.handleMessage(ctx, msg);
    }

}
