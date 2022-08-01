package com.lhf.game.map;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import com.lhf.game.creature.Player;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.GoMessage;
import com.lhf.messages.in.ShoutMessage;
import com.lhf.messages.out.BadGoMessage;
import com.lhf.messages.out.BadGoMessage.BadGoType;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.ReincarnateMessage;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.messages.out.SeeOutMessage.SeeCategory;
import com.lhf.messages.out.SpawnMessage;
import com.lhf.messages.out.SpeakingMessage;
import com.lhf.server.client.user.UserID;

public class Dungeon implements MessageHandler {
    private Map<Room, Map<Directions, Room>> mapping;
    private Room startingRoom = null;
    private MessageHandler successor;
    private Map<CommandMessage, String> commands;

    Dungeon(MessageHandler successor) {
        this.mapping = new HashMap<>();
        this.successor = successor;
        this.commands = this.buildCommands();
    }

    private Map<CommandMessage, String> buildCommands() {
        StringBuilder sb = new StringBuilder();
        Map<CommandMessage, String> cmds = new HashMap<>();
        sb.append("\"shout [message]\" ").append("Tells everyone in the dungeon your message!");
        cmds.put(CommandMessage.SHOUT, sb.toString());
        sb.setLength(0);
        sb.append("\"go [direction]\"")
                .append("Move in the desired direction, if that direction exists.  Like \"go east\"");
        cmds.put(CommandMessage.GO, sb.toString());
        return cmds;
    }

    public boolean addNewPlayer(Player p) {
        this.startingRoom.sendMessageToAll(new SpawnMessage(p.getColorTaggedName()));
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
        p.sendMsg(new ReincarnateMessage(p.getColorTaggedName()));
        p2.sendMsg(new SeeOutMessage(startingRoom));
    }

    void setStartingRoom(Room r) {
        this.startingRoom = r;
        this.mapping.putIfAbsent(r, new TreeMap<>());
        r.setDungeon(this);
        r.setSuccessor(this);
    }

    boolean connectRoom(Room existing, Directions toExistingRoom, Room tooAdd) {
        if (!this.connectRoomOneWay(existing, toExistingRoom, tooAdd)) {
            return false;
        }
        Map<Directions, Room> existingExits = this.mapping.get(existing);
        if (existingExits.containsKey(toExistingRoom.opposite())) {
            return false;
        }
        return existingExits.put(toExistingRoom.opposite(), tooAdd) == null;
    }

    boolean connectRoomOneWay(Room existing, Directions toExistingRoom, Room secretRoom) {
        if (!this.mapping.containsKey(existing)) {
            return false;
        }
        secretRoom.setDungeon(this);
        secretRoom.setSuccessor(this);
        this.mapping.putIfAbsent(secretRoom, new TreeMap<>());
        Map<Directions, Room> tooAddExits = this.mapping.get(secretRoom);
        if (tooAddExits.containsKey(toExistingRoom)) {
            return false;
        }
        return tooAddExits.put(toExistingRoom, existing) == null;
    }

    public Room getPlayerRoom(UserID id) {
        for (Room r : this.mapping.keySet()) {
            Player p = r.getPlayerInRoom(id);
            if (p != null) {
                return r;
            }
        }
        return null;
    }

    public Player getPlayerById(UserID id) {
        for (Room r : this.mapping.keySet()) {
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
        if (!this.mapping.containsKey(this.startingRoom)) {
            this.sendMessageToAllInRoom(this.startingRoom, msg);
        }
        for (Room room : this.mapping.keySet()) {
            this.sendMessageToAllInRoom(room, msg);
        }
    }

    public void sendMessageToAllExcept(OutMessage msg, String... exactNames) {
        if (!this.mapping.containsKey(this.startingRoom)) {
            this.sendMessageToAllInRoomExcept(this.startingRoom, msg, exactNames);
        }
        for (Room room : this.mapping.keySet()) {
            this.sendMessageToAllInRoomExcept(room, msg, exactNames);
        }
    }

    private Boolean handleShout(CommandContext ctx, Command cmd) {
        if (cmd.getType() == CommandMessage.SHOUT) {
            ShoutMessage shoutMessage = (ShoutMessage) cmd;
            for (Room room : this.mapping.keySet()) {
                for (Player p : room.getAllPlayersInRoom()) {
                    p.sendMsg(new SpeakingMessage(ctx.getCreature(), true, shoutMessage.getMessage()));
                }
            }
            return true;
        }
        return false;
    }

    private Boolean handleGo(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.GO) {
            GoMessage goMessage = (GoMessage) msg;
            if (ctx.getRoom() == null) {
                ctx.sendMsg(new BadGoMessage(BadGoType.NO_ROOM, goMessage.getDirection()));
                return true;
            }
            Room presentRoom = ctx.getRoom();
            if (this.mapping.containsKey(presentRoom)) {
                Map<Directions, Room> exits = this.mapping.get(presentRoom);
                if (exits == null || exits.size() == 0 || !exits.containsKey(goMessage.getDirection())
                        || exits.get(goMessage.getDirection()) == null) {
                    ctx.sendMsg(new BadGoMessage(BadGoType.DNE, goMessage.getDirection()));
                    return true;
                }
                Room nextRoom = exits.get(goMessage.getDirection());
                ctx.getCreature().setSuccessor(nextRoom);
                nextRoom.addCreature(ctx.getCreature());
                presentRoom.removeCreature(ctx.getCreature());
                return true;
            } else {
                ctx.sendMsg(new BadGoMessage(BadGoType.NO_ROOM, goMessage.getDirection()));
                return true;
            }
        }
        return false;
    }

    public SeeOutMessage seeRoomExits(Room room) {
        if (room == null) {
            return new SeeOutMessage("You are not in a room, so you can't see much.");
        }
        SeeOutMessage roomSeen = room.produceMessage();
        if (this.mapping.containsKey(room)) {
            Map<Directions, Room> presentExits = this.mapping.get(room);
            if (presentExits != null) {
                for (Directions dir : presentExits.keySet()) {
                    roomSeen.addSeen(SeeCategory.DIRECTION, dir);
                }
            }
        } else {
            roomSeen.addExtraInfo("But this room is not in a proper dungeon.");
        }
        return roomSeen;
    }

    private Boolean handleSee(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.SEE) {
            Room presentRoom = ctx.getRoom();
            SeeOutMessage roomSeen = this.seeRoomExits(presentRoom);
            ctx.sendMsg(roomSeen);
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
        } else if (msg.getType() == CommandMessage.GO) {
            performed = this.handleGo(ctx, msg);
        } else if (msg.getType() == CommandMessage.SEE) {
            performed = this.handleSee(ctx, msg);
        }
        if (performed) {
            return performed;
        }
        return MessageHandler.super.handleMessage(ctx, msg);
    }

    public String toMermaid(boolean fence) {
        StringBuilder sb = new StringBuilder();
        StringBuilder edges = new StringBuilder();
        if (fence) {
            sb.append("```mermaid").append("\r\n");
        }
        sb.append("flowchart LR").append("\r\n");
        for (Map.Entry<Room, Map<Directions, Room>> mappingEntry : this.mapping.entrySet()) {
            Room room = mappingEntry.getKey();
            String editUUID = room.getUuid().toString();
            sb.append("    ").append(editUUID).append("[").append(room.getName()).append("]\r\n");
            for (Map.Entry<Directions, Room> exits : mappingEntry.getValue().entrySet()) {
                String otherUUID = exits.getValue().getUuid().toString();
                edges.append("    ").append(editUUID).append("-->|").append(exits.getKey().toString()).append("|")
                        .append(otherUUID).append("\r\n");
            }
        }
        sb.append("\r\n");
        sb.append(edges.toString());
        if (fence) {
            sb.append("```").append("\r\n");
        }
        return sb.toString();
    }

}
