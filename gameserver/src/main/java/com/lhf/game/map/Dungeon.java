package com.lhf.game.map;

import java.util.*;
import java.util.Map.Entry;

import com.lhf.game.AffectableEntity;
import com.lhf.game.CreatureContainer;
import com.lhf.game.EntityEffect;
import com.lhf.game.creature.Creature;
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
import com.lhf.messages.out.BadMessage;
import com.lhf.messages.out.BadMessage.BadMessageType;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.ReincarnateMessage;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.messages.out.SeeOutMessage.SeeCategory;
import com.lhf.messages.out.SpawnMessage;
import com.lhf.messages.out.SpeakingMessage;
import com.lhf.server.client.user.UserID;

public class Dungeon implements CreatureContainer, MessageHandler, AffectableEntity<DungeonEffect> {
    public class RoomAndDirs {
        public final Room room;
        public Map<Directions, Doorway> exits;

        // package private
        RoomAndDirs(Room room) {
            this.room = room;
            this.exits = new TreeMap<>();
        }
    }

    private Map<UUID, RoomAndDirs> mapping;
    private Room startingRoom = null;
    private MessageHandler successor;
    private Map<CommandMessage, String> commands;
    private transient TreeSet<DungeonEffect> effects;

    Dungeon() {
        this.mapping = new TreeMap<>();
        this.successor = null;
        this.commands = this.buildCommands();
        this.effects = new TreeSet<>();
    }

    Dungeon(MessageHandler successor) {
        this.mapping = new TreeMap<>();
        this.successor = successor;
        this.commands = this.buildCommands();
        this.effects = new TreeSet<>();
    }

    private Map<CommandMessage, String> buildCommands() {
        StringBuilder sb = new StringBuilder();
        Map<CommandMessage, String> cmds = new EnumMap<>(CommandMessage.class);
        sb.append("\"shout [message]\" ").append("Tells everyone in the dungeon your message!");
        cmds.put(CommandMessage.SHOUT, sb.toString());
        sb.setLength(0);
        sb.append("\"go [direction]\"")
                .append("Move in the desired direction, if that direction exists.  Like \"go east\"");
        cmds.put(CommandMessage.GO, sb.toString());
        return cmds;
    }

    @Override
    public Collection<Creature> getCreatures() {
        Set<Creature> creatures = new TreeSet<>();
        if (this.startingRoom != null) {
            creatures.addAll(this.startingRoom.getCreatures());
        }
        for (RoomAndDirs rAndD : this.mapping.values()) {
            if (rAndD.room != null) {
                creatures.addAll(rAndD.room.getCreatures());
            }
        }
        return Collections.unmodifiableSet(creatures);
    }

    @Override
    public boolean addPlayer(Player player) {
        this.startingRoom.announce(new SpawnMessage(player.getColorTaggedName()));
        player.setSuccessor(this);
        return startingRoom.addPlayer(player);
    }

    @Override
    public Optional<Player> removePlayer(UserID id) {
        Room room = this.getPlayerRoom(id);
        if (room == null) {
            return Optional.empty();
        }
        return room.removePlayer(id);
    }

    @Override
    public boolean removePlayer(Player player) {
        return this.removePlayer(player.getId()).isPresent();
    }

    public Room getPlayerRoom(UserID id) {
        for (RoomAndDirs rAndD : this.mapping.values()) {
            Optional<Player> found = rAndD.room.getPlayer(id);
            if (found.isPresent()) {
                return rAndD.room;
            }
        }
        return null;
    }

    public Room getCreatureRoom(Creature creature) {
        for (RoomAndDirs rAndD : this.mapping.values()) {
            if (rAndD.room.hasCreature(creature)) {
                return rAndD.room;
            }
        }
        return null;
    }

    public Room getCreatureRoom(String name) {
        for (RoomAndDirs rAndD : this.mapping.values()) {
            if (rAndD.room.hasCreature(name, null)) {
                return rAndD.room;
            }
        }
        return null;
    }

    @Override
    public Optional<Player> getPlayer(UserID id) {
        for (RoomAndDirs rAndD : this.mapping.values()) {
            Optional<Player> found = rAndD.room.getPlayer(id);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean addCreature(Creature creature) {
        this.startingRoom.announce(new SpawnMessage(creature.getColorTaggedName()));
        creature.setSuccessor(this);
        return startingRoom.addCreature(creature);
    }

    public boolean addCreature(Creature creature, UUID roomUUID) {
        if (this.mapping.containsKey(roomUUID)) {
            RoomAndDirs roomAndDirs = this.mapping.get(roomUUID);
            roomAndDirs.room.announce(new SpawnMessage(creature.getColorTaggedName()));
            creature.setSuccessor(this);
            return roomAndDirs.room.addCreature(creature);
        }
        return false;
    }

    @Override
    public Optional<Creature> removeCreature(String name) {
        Room room = this.getCreatureRoom(name);
        if (room == null) {
            return Optional.empty();
        }
        return room.removeCreature(name);
    }

    @Override
    public boolean removeCreature(Creature creature) {
        Room room = this.getCreatureRoom(creature);
        if (room == null) {
            return false;
        }
        return room.removeCreature(creature);
    }

    @Override
    public Optional<Player> removePlayer(String name) {
        for (RoomAndDirs rAndD : this.mapping.values()) {
            Optional<Player> found = rAndD.room.removePlayer(name);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    public Set<UserID> getPlayersInRoom(UserID id) {
        Collection<Creature> players = Objects.requireNonNull(getPlayerRoom(id)).getPlayers();
        Set<UserID> ids = new TreeSet<>();
        players.forEach(player -> {
            if (player instanceof Player) {
                ids.add(((Player) player).getId());
            }
        });
        return ids;
    }

    void reincarnate(Player p) {
        Player p2 = new Player(p.getUser());
        addPlayer(p2);
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

    public RoomAndDirs getRoomExits(Room room) {
        return this.mapping.get(room.getUuid());
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

    public void announceToAllInRoom(Room room, OutMessage msg, String... deafened) {
        if (room == null) {
            this.startingRoom.announce(msg, deafened);
            return;
        }
        room.announce(msg, deafened);
    }

    private Boolean handleShout(CommandContext ctx, Command cmd) {
        if (cmd.getType() == CommandMessage.SHOUT) {
            if (ctx.getCreature() == null) {
                ctx.sendMsg(new BadMessage(BadMessageType.CREATURES_ONLY, this.gatherHelp(ctx), cmd));
                return true;
            }
            ShoutMessage shoutMessage = (ShoutMessage) cmd;
            this.announceDirect(new SpeakingMessage(ctx.getCreature(), true, shoutMessage.getMessage()),
                    this.getPlayers());
            return true;
        }
        return false;
    }

    private Boolean handleGo(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.GO) {
            if (ctx.getCreature() == null) {
                ctx.sendMsg(new BadMessage(BadMessageType.CREATURES_ONLY, this.gatherHelp(ctx), msg));
                return true;
            }
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
                presentRoom.removeCreature(ctx.getCreature(), toGo);
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
        return Collections.unmodifiableMap(this.commands);
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        return ctx;
    }

    @Override
    public EnumMap<CommandMessage, String> gatherHelp(CommandContext ctx) {
        EnumMap<CommandMessage, String> gathered = MessageHandler.super.gatherHelp(ctx);
        if (ctx.getCreature() == null) {
            gathered.remove(CommandMessage.SHOUT);
            gathered.remove(CommandMessage.GO);
        }
        if (ctx.getRoom() == null) {
            gathered.remove(CommandMessage.GO);
        }
        return gathered;
    }

    @Override
    public boolean handleMessage(CommandContext ctx, Command msg) {
        Boolean performed = false;
        ctx = this.addSelfToContext(ctx);
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

    @Override
    public NavigableSet<DungeonEffect> getMutableEffects() {
        return this.effects;
    }

    @Override
    public boolean isCorrectEffectType(EntityEffect effect) {
        return effect instanceof DungeonEffect;
    }

    @Override
    public OutMessage processEffect(EntityEffect effect, boolean reverse) {
        // TODO make effects applicable here
        return null;
    }

    @Override
    public String getName() {
        // TODO: do dungeons need names?
        return "Ibaif";
    }

    @Override
    public String printDescription() {
        return String.format("This Dungeon is called %s and it has %d rooms!", this.getName(), this.mapping.size());
    }

    @Override
    public SeeOutMessage produceMessage() {
        return new SeeOutMessage(this);
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
