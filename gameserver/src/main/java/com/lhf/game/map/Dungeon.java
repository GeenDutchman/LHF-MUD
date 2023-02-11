package com.lhf.game.map;

import java.util.*;
import java.util.Map.Entry;

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

public class Dungeon implements Land {
    public class AreaAndDirs implements Land.AreaDirectionalLinks {
        public final Area room;
        public Map<Directions, Doorway> exits;

        // package private
        AreaAndDirs(Area room) {
            this.room = room;
            this.exits = new TreeMap<>();
        }

        @Override
        public Area getArea() {
            return this.room;
        }

        @Override
        public Map<Directions, Doorway> getExits() {
            return this.exits;
        }
    }

    private Map<UUID, Land.AreaDirectionalLinks> mapping;
    private Area startingRoom = null;
    private MessageHandler successor;
    private Map<CommandMessage, String> commands;
    private transient TreeSet<DungeonEffect> effects;

    Dungeon(Land.LandBuilder builder) {
        this.startingRoom = builder.getStartingArea();
        this.mapping = builder.getAtlas();
        this.successor = builder.getSuccessor();
        this.commands = this.buildCommands();
        this.effects = new TreeSet<>();
    }

    @Override
    public Map<UUID, Land.AreaDirectionalLinks> getAtlas() {
        return this.mapping;
    }

    @Override
    public Area getStartingArea() {
        return this.startingRoom;
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
    public boolean addPlayer(Player player) {
        this.startingRoom.announce(new SpawnMessage(player.getColorTaggedName()));
        player.setSuccessor(this);
        return startingRoom.addPlayer(player);
    }

    @Override
    public Optional<Player> removePlayer(UserID id) {
        Area room = this.getPlayerArea(id);
        if (room == null) {
            return Optional.empty();
        }
        return room.removePlayer(id);
    }

    @Override
    public boolean removePlayer(Player player) {
        return this.removePlayer(player.getId()).isPresent();
    }

    @Override
    public Optional<Player> getPlayer(UserID id) {
        for (AreaDirectionalLinks rAndD : this.mapping.values()) {
            Optional<Player> found = rAndD.getArea().getPlayer(id);
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
            AreaDirectionalLinks roomAndDirs = this.mapping.get(roomUUID);
            roomAndDirs.getArea().announce(new SpawnMessage(creature.getColorTaggedName()));
            creature.setSuccessor(this);
            return roomAndDirs.getArea().addCreature(creature);
        }
        return false;
    }

    @Override
    public Optional<Creature> removeCreature(String name) {
        Area room = this.getCreatureArea(name);
        if (room == null) {
            return Optional.empty();
        }
        return room.removeCreature(name);
    }

    @Override
    public boolean removeCreature(Creature creature) {
        Area room = this.getCreatureArea(creature);
        if (room == null) {
            return false;
        }
        return room.removeCreature(creature);
    }

    @Override
    public Optional<Player> removePlayer(String name) {
        for (AreaDirectionalLinks rAndD : this.mapping.values()) {
            Optional<Player> found = rAndD.getArea().removePlayer(name);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    public Set<UserID> getPlayersInRoom(UserID id) {
        Collection<Creature> players = Objects.requireNonNull(getPlayerArea(id)).getPlayers();
        Set<UserID> ids = new TreeSet<>();
        players.forEach(player -> {
            if (player instanceof Player) {
                ids.add(((Player) player).getId());
            }
        });
        return ids;
    }

    @Override
    public boolean onCreatureDeath(Creature creature) {
        if (this.removeCreature(creature)) {
            if (creature != null && creature instanceof Player) {
                Player asPlayer = (Player) creature;
                Player nextLife = Player.PlayerBuilder.getInstance(asPlayer.getUser()).build();
                this.addPlayer(nextLife);
                creature.sendMsg(new ReincarnateMessage(creature.getColorTaggedName()));
                nextLife.sendMsg(new SeeOutMessage(startingRoom));

            }
            return true;
        }
        return false;
    }

    void setStartingRoom(Room r) {
        this.startingRoom = r;
        this.mapping.putIfAbsent(r.getUuid(), new AreaAndDirs(r));
        r.setDungeon(this);
        r.setSuccessor(this);
    }

    private boolean basicAddRoom(Room existing, Room toAdd) {
        if (!this.mapping.containsKey(existing.getUuid())) {
            return false;
        }
        toAdd.setDungeon(this);
        toAdd.setSuccessor(this);
        this.mapping.putIfAbsent(toAdd.getUuid(), new AreaAndDirs(toAdd));
        return true;
    }

    public AreaDirectionalLinks getRoomExits(Room room) {
        return this.mapping.get(room.getUuid());
    }

    boolean connectRoom(DoorwayType type, Room toAdd, Directions toExistingRoom, Room existing) {
        if (!this.basicAddRoom(existing, toAdd)) {
            return false;
        }
        Doorway doorway = DoorwayFactory.createDoorway(type, toAdd, toExistingRoom, existing);
        AreaDirectionalLinks addedDirs = this.mapping.get(toAdd.getUuid());
        if (addedDirs.getExits().containsKey(toExistingRoom)) {
            return false;
        }
        AreaDirectionalLinks existingDirs = this.mapping.get(existing.getUuid());
        if (existingDirs.getExits().containsKey(toExistingRoom.opposite())) {
            return false;
        }
        return addedDirs.getExits().put(toExistingRoom, doorway) == null &&
                existingDirs.getExits().put(toExistingRoom.opposite(), doorway) == null;
    }

    boolean connectRoomExclusiveOneWay(Room secretRoom, Directions toExistingRoom, Room existing) {
        if (!this.basicAddRoom(existing, secretRoom)) {
            return false;
        }
        AreaDirectionalLinks secretDirs = this.mapping.get(secretRoom.getUuid());
        if (secretDirs.getExits().containsKey(toExistingRoom)) {
            return false;
        }
        Doorway onewayDoor = DoorwayFactory.createDoorway(DoorwayType.ONE_WAY, secretRoom, toExistingRoom, existing);
        return secretDirs.getExits().put(toExistingRoom, onewayDoor) == null;
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
                AreaDirectionalLinks roomAndDirs = this.mapping.get(presentRoom.getUuid());
                Map<Directions, Doorway> exits = roomAndDirs.getExits();
                if (exits == null || exits.size() == 0
                        || !exits.containsKey(toGo)
                        || exits.get(toGo) == null) {
                    ctx.sendMsg(new BadGoMessage(BadGoType.DNE, toGo));
                    return true;
                }
                Doorway doorway = exits.get(toGo);
                if (!doorway.canTraverse(ctx.getCreature(), toGo)) {
                    ctx.sendMsg(new BadGoMessage(BadGoType.BLOCKED, toGo, exits.keySet()));
                    return true;
                }
                UUID nextRoomUuid = doorway.getRoomAccross(presentRoom.getUuid());
                AreaDirectionalLinks nextRandD = this.mapping.get(nextRoomUuid);
                if (nextRoomUuid == null || nextRandD == null) {
                    ctx.sendMsg(new BadGoMessage(BadGoType.DNE, toGo, exits.keySet()));
                    return true;
                }
                Area nextRoom = nextRandD.getArea();
                if (nextRoom == null) {
                    ctx.sendMsg(new BadGoMessage(BadGoType.DNE, toGo, exits.keySet()));
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

    private Boolean handleSee(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.SEE) {
            Room presentRoom = ctx.getRoom();
            if (presentRoom != null) {
                SeeOutMessage roomSeen = presentRoom.produceMessage();
                ctx.sendMsg(roomSeen);
                return true;
            }
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
        EnumMap<CommandMessage, String> gathered = Land.super.gatherHelp(ctx);
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
        return Land.super.handleMessage(ctx, msg);
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
        for (AreaDirectionalLinks roomAndDirs : this.mapping.values()) {
            Area room = roomAndDirs.getArea();
            String editUUID = room.getUuid().toString();
            sb.append("    ").append(editUUID).append("[").append(room.getName()).append("]\r\n");
            for (Entry<Directions, Doorway> exits : roomAndDirs.getExits().entrySet()) {
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
