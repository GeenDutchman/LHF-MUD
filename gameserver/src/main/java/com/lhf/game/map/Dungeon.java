package com.lhf.game.map;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;

import com.lhf.game.creature.Player;
import com.lhf.game.map.DoorwayFactory.DoorwayType;
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
    private class RoomAndDirs {
        public final Room room;
        public Map<Directions, Doorway> exits;

        RoomAndDirs(Room room) {
            this.room = room;
            this.exits = new TreeMap<>();
        }
    }

    private Map<UUID, RoomAndDirs> mapping;
    private Room startingRoom = null;
    private MessageHandler successor;
    private Map<CommandMessage, String> commands;

    Dungeon(MessageHandler successor) {
        this.mapping = new TreeMap<>();
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
        this.mapping.putIfAbsent(r.getUuid(), new RoomAndDirs(r));
        r.setDungeon(this);
        r.setSuccessor(this);
    }

    private boolean basicAddRoom(Room existing, Room toAdd) {
        if (!this.mapping.containsKey(existing.getUuid())) {
            return false;
        }
        toAdd.setDungeon(this);
        toAdd.setSuccessor(this);
        this.mapping.putIfAbsent(toAdd.getUuid(), new RoomAndDirs(toAdd));
        return true;
    }

    boolean connectRoom(DoorwayType type, Room toAdd, Directions toExistingRoom, Room existing) {
        if (!this.basicAddRoom(existing, toAdd)) {
            return false;
        }
        Doorway doorway = DoorwayFactory.createDoorway(type, toAdd, toExistingRoom, existing);
        RoomAndDirs addedDirs = this.mapping.get(toAdd.getUuid());
        if (addedDirs.exits.containsKey(toExistingRoom)) {
            return false;
        }
        RoomAndDirs existingDirs = this.mapping.get(existing.getUuid());
        if (existingDirs.exits.containsKey(toExistingRoom.opposite())) {
            return false;
        }
        return addedDirs.exits.put(toExistingRoom, doorway) == null &&
                existingDirs.exits.put(toExistingRoom.opposite(), doorway) == null;
    }

    boolean connectRoomExclusiveOneWay(Room secretRoom, Directions toExistingRoom, Room existing) {
        if (!this.basicAddRoom(existing, secretRoom)) {
            return false;
        }
        RoomAndDirs secretDirs = this.mapping.get(secretRoom.getUuid());
        if (secretDirs.exits.containsKey(toExistingRoom)) {
            return false;
        }
        Doorway onewayDoor = DoorwayFactory.createDoorway(DoorwayType.ONE_WAY, secretRoom, toExistingRoom, existing);
        return secretDirs.exits.put(toExistingRoom, onewayDoor) == null;
    }

    public Room getPlayerRoom(UserID id) {
        for (RoomAndDirs rAndD : this.mapping.values()) {
            Player p = rAndD.room.getPlayerInRoom(id);
            if (p != null) {
                return rAndD.room;
            }
        }
        return null;
    }

    public Player getPlayerById(UserID id) {
        for (RoomAndDirs rAndD : this.mapping.values()) {
            Player p = rAndD.room.getPlayerInRoom(id);
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
        if (!this.mapping.containsKey(this.startingRoom.getUuid())) {
            this.sendMessageToAllInRoom(this.startingRoom, msg);
        }
        for (RoomAndDirs rAndD : this.mapping.values()) {
            this.sendMessageToAllInRoom(rAndD.room, msg);
        }
    }

    public void sendMessageToAllExcept(OutMessage msg, String... exactNames) {
        if (!this.mapping.containsKey(this.startingRoom.getUuid())) {
            this.sendMessageToAllInRoomExcept(this.startingRoom, msg, exactNames);
        }
        for (RoomAndDirs rAndD : this.mapping.values()) {
            this.sendMessageToAllInRoomExcept(rAndD.room, msg, exactNames);
        }
    }

    private Boolean handleShout(CommandContext ctx, Command cmd) {
        if (cmd.getType() == CommandMessage.SHOUT) {
            ShoutMessage shoutMessage = (ShoutMessage) cmd;
            for (RoomAndDirs rAndD : this.mapping.values()) {
                for (Player p : rAndD.room.getAllPlayersInRoom()) {
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
            Directions toGo = goMessage.getDirection();
            if (ctx.getRoom() == null) {
                ctx.sendMsg(new BadGoMessage(BadGoType.NO_ROOM, toGo));
                return true;
            }
            Room presentRoom = ctx.getRoom();
            if (this.mapping.containsKey(presentRoom.getUuid())) {
                RoomAndDirs roomAndDirs = this.mapping.get(presentRoom.getUuid());
                if (roomAndDirs.exits == null || roomAndDirs.exits.size() == 0
                        || !roomAndDirs.exits.containsKey(toGo)
                        || roomAndDirs.exits.get(toGo) == null) {
                    ctx.sendMsg(new BadGoMessage(BadGoType.DNE, toGo));
                    return true;
                }
                Doorway doorway = roomAndDirs.exits.get(toGo);
                if (!doorway.canTraverse(ctx.getCreature(), toGo)) {
                    ctx.sendMsg(new BadGoMessage(BadGoType.BLOCKED, toGo, roomAndDirs.exits.keySet()));
                    return true;
                }
                UUID nextRoomUuid = doorway.getRoomAccross(presentRoom.getUuid());
                RoomAndDirs nextRandD = this.mapping.get(nextRoomUuid);
                if (nextRoomUuid == null || nextRandD == null) {
                    ctx.sendMsg(new BadGoMessage(BadGoType.DNE, toGo, roomAndDirs.exits.keySet()));
                    return true;
                }
                Room nextRoom = nextRandD.room;
                if (nextRoom == null) {
                    ctx.sendMsg(new BadGoMessage(BadGoType.DNE, toGo, roomAndDirs.exits.keySet()));
                    return true;
                }
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
        if (this.mapping.containsKey(room.getUuid())) {
            RoomAndDirs rAndD = this.mapping.get(room.getUuid());
            if (rAndD.exits != null) {
                for (Directions dir : rAndD.exits.keySet()) {
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
        for (RoomAndDirs roomAndDirs : this.mapping.values()) {
            Room room = roomAndDirs.room;
            String editUUID = room.getUuid().toString();
            sb.append("    ").append(editUUID).append("[").append(room.getName()).append("]\r\n");
            for (Entry<Directions, Doorway> exits : roomAndDirs.exits.entrySet()) {
                String otherUUID = exits.getValue().getRoomAccross(room.getUuid()).toString();
                edges.append("    ").append(otherUUID).append("-->|").append(exits.getKey().toString()).append("|")
                        .append(editUUID).append("\r\n");
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
