package com.lhf.game.map;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.EntityEffect;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.Player;
import com.lhf.game.map.DoorwayFactory.DoorwayType;
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.BadGoEvent;
import com.lhf.messages.events.BadMessageEvent;
import com.lhf.messages.events.CreatureSpawnedEvent;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.PlayerReincarnatedEvent;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.events.SpeakingEvent;
import com.lhf.messages.events.BadGoEvent.BadGoType;
import com.lhf.messages.events.BadMessageEvent.BadMessageType;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageChainHandler;
import com.lhf.messages.in.GoMessage;
import com.lhf.messages.in.ShoutMessage;
import com.lhf.server.client.ClientID;
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
    private transient MessageChainHandler successor;
    private Map<CommandMessage, CommandHandler> commands;
    private transient TreeSet<DungeonEffect> effects;
    private transient final Logger logger;
    private final ClientID clientID;

    Dungeon(Land.LandBuilder builder) {
        this.logger = Logger.getLogger(String.format("%s.%s", this.getClass().getName(), this.getName()));
        this.clientID = new ClientID();
        this.startingRoom = builder.getStartingArea();
        this.mapping = builder.getAtlas();
        this.successor = builder.getSuccessor();
        for (AreaDirectionalLinks links : this.mapping.values()) {
            Area area = links.getArea();
            if (area != null) {
                area.setLand(this);
                area.setSuccessor(this);
            }
        }
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

    private Map<CommandMessage, CommandHandler> buildCommands() {
        Map<CommandMessage, CommandHandler> cmds = new EnumMap<>(CommandMessage.class);
        cmds.put(CommandMessage.SHOUT, new ShoutHandler());
        cmds.put(CommandMessage.GO, new GoHandler());
        cmds.put(CommandMessage.SEE, new SeeHandler());
        return cmds;
    }

    @Override
    public ClientID getClientID() {
        return this.clientID;
    }

    @Override
    public boolean addPlayer(Player player) {
        this.startingRoom
                .announce(CreatureSpawnedEvent.getBuilder().setBroacast().setCreatureName(player.getColorTaggedName())
                        .Build());
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
    public boolean addCreature(ICreature creature) {
        this.startingRoom
                .announce(CreatureSpawnedEvent.getBuilder().setCreatureName(creature.getColorTaggedName()).setBroacast()
                        .Build());
        creature.setSuccessor(this);
        return startingRoom.addCreature(creature);
    }

    public boolean addCreature(ICreature creature, UUID roomUUID) {
        if (this.mapping.containsKey(roomUUID)) {
            AreaDirectionalLinks roomAndDirs = this.mapping.get(roomUUID);
            roomAndDirs.getArea()
                    .announce(CreatureSpawnedEvent.getBuilder().setCreatureName(creature.getColorTaggedName()).Build());
            creature.setSuccessor(this);
            return roomAndDirs.getArea().addCreature(creature);
        }
        return false;
    }

    @Override
    public Optional<ICreature> removeCreature(String name) {
        Area room = this.getCreatureArea(name);
        if (room == null) {
            return Optional.empty();
        }
        return room.removeCreature(name);
    }

    @Override
    public boolean removeCreature(ICreature creature) {
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
        Collection<ICreature> players = Objects.requireNonNull(getPlayerArea(id)).getPlayers();
        Set<UserID> ids = new TreeSet<>();
        players.forEach(player -> {
            if (player instanceof Player) {
                ids.add(((Player) player).getId());
            }
        });
        return ids;
    }

    @Override
    public boolean onCreatureDeath(ICreature creature) {
        boolean removed = this.removeCreature(creature);

        if (creature != null && creature instanceof Player) {
            Player asPlayer = (Player) creature;
            Player nextLife = Player.PlayerBuilder.getInstance(asPlayer.getUser()).setVocation(asPlayer.getVocation())
                    .build();
            this.addPlayer(nextLife);
            ICreature.eventAccepter.accept(creature,
                    PlayerReincarnatedEvent.getBuilder().setTaggedName(creature).setNotBroadcast().Build());
            ICreature.eventAccepter.accept(nextLife, SeeEvent.getBuilder().setExaminable(startingRoom).Build());
        }
        return removed;
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

    public void announceToAllInRoom(Room room, GameEvent event, ClientMessenger... deafened) {
        if (room == null) {
            this.startingRoom.announce(event, deafened);
            return;
        }
        room.announce(event, deafened);
    }

    public interface DungeonCommandHandler extends Room.RoomCommandHandler {
        final static Predicate<CommandContext> defaultDungeonPredicate = DungeonCommandHandler.defaultRoomPredicate
                .and(ctx -> ctx.getDungeon() != null);
    }

    protected class ShoutHandler implements DungeonCommandHandler {
        private final static String helpString = "\"shout [message]\" Tells everyone in the dungeon your message!";

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.SHOUT;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(ShoutHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return ShoutHandler.defaultDungeonPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.SHOUT && cmd instanceof ShoutMessage shoutMessage) {
                if (ctx.getCreature() == null) {
                    ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                            .setHelps(ctx.getHelps()).setCommand(cmd).Build());
                    return ctx.handled();
                }
                Dungeon.this.announceDirect(
                        SpeakingEvent.getBuilder().setSayer(ctx.getCreature()).setShouting(true)
                                .setMessage(shoutMessage.getMessage()).Build(),
                        Dungeon.this.getPlayers().stream().filter(player -> player != null)
                                .map(player -> (ClientMessenger) player)
                                .toList());
                return ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public MessageChainHandler getChainHandler() {
            return Dungeon.this;
        }

    }

    protected class GoHandler implements DungeonCommandHandler {
        private final static String helpString = "\"go [direction]\" Move in the desired direction, if that direction exists.  Like \"go east\"";

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.GO;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(GoHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return GoHandler.defaultDungeonPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.GO && cmd instanceof GoMessage goMessage) {
                if (ctx.getCreature() == null) {
                    ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                            .setHelps(ctx.getHelps()).setCommand(cmd).Build());
                    return ctx.handled();
                }
                Directions toGo = goMessage.getDirection();
                if (ctx.getRoom() == null) {
                    ctx.receive(BadGoEvent.getBuilder().setSubType(BadGoType.NO_ROOM).setAttempted(toGo).Build());
                    return ctx.handled();
                }
                Room presentRoom = ctx.getRoom();
                if (Dungeon.this.mapping.containsKey(presentRoom.getUuid())) {
                    AreaDirectionalLinks roomAndDirs = Dungeon.this.mapping.get(presentRoom.getUuid());
                    Map<Directions, Doorway> exits = roomAndDirs.getExits();
                    if (exits == null || exits.size() == 0
                            || !exits.containsKey(toGo)
                            || exits.get(toGo) == null) {
                        ctx.receive(BadGoEvent.getBuilder().setSubType(BadGoType.DNE).setAttempted(toGo).Build());
                        return ctx.handled();
                    }
                    Doorway doorway = exits.get(toGo);
                    if (!doorway.canTraverse(ctx.getCreature(), toGo)) {
                        ctx.receive(BadGoEvent.getBuilder().setSubType(BadGoType.BLOCKED).setAttempted(toGo)
                                .setAvailable(exits.keySet()).Build());
                        return ctx.handled();
                    }
                    UUID nextRoomUuid = doorway.getRoomAccross(presentRoom.getUuid());
                    AreaDirectionalLinks nextRandD = Dungeon.this.mapping.get(nextRoomUuid);
                    if (nextRoomUuid == null || nextRandD == null) {
                        ctx.receive(BadGoEvent.getBuilder().setSubType(BadGoType.DNE).setAttempted(toGo)
                                .setAvailable(exits.keySet()).Build());
                        return ctx.handled();
                    }
                    Area nextRoom = nextRandD.getArea();
                    if (nextRoom == null) {
                        ctx.receive(BadGoEvent.getBuilder().setSubType(BadGoType.DNE).setAttempted(toGo)
                                .setAvailable(exits.keySet()).Build());
                        return ctx.handled();
                    }

                    ctx.getCreature().setSuccessor(nextRoom);
                    nextRoom.addCreature(ctx.getCreature());
                    presentRoom.removeCreature(ctx.getCreature(), toGo);
                    return ctx.handled();
                } else {
                    ctx.receive(BadGoEvent.getBuilder().setSubType(BadGoType.NO_ROOM)
                            .setAttempted(goMessage.getDirection()).Build());
                    return ctx.handled();
                }
            }
            return ctx.failhandle();
        }

        @Override
        public MessageChainHandler getChainHandler() {
            return Dungeon.this;
        }

    }

    protected class SeeHandler implements DungeonCommandHandler {
        private final static String helpString = new StringJoiner(" ")
                .add("\"see\"").add("Will give you some information about your surroundings.\r\n")
                .add("\"see [name]\"").add("May tell you more about the object with that name.")
                .toString();

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.SEE;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(SeeHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return SeeHandler.defaultDungeonPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.SEE) {
                Room presentRoom = ctx.getRoom();
                if (presentRoom != null) {
                    SeeEvent roomSeen = presentRoom.produceMessage();
                    ctx.receive(roomSeen);
                    return ctx.handled();
                }
            }
            return ctx.failhandle();
        }

        @Override
        public MessageChainHandler getChainHandler() {
            return Dungeon.this;
        }

    }

    @Override
    public void setSuccessor(MessageChainHandler successor) {
        this.successor = successor;
    }

    @Override
    public MessageChainHandler getSuccessor() {
        return this.successor;
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        ctx.setDungeon(this);
        return ctx;
    }

    @Override
    public Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx) {
        return Collections.unmodifiableMap(this.commands);
    }

    @Override
    public synchronized void log(Level logLevel, String logMessage) {
        this.logger.log(logLevel, logMessage);

    }

    @Override
    public synchronized void log(Level logLevel, Supplier<String> logMessageSupplier) {
        this.logger.log(logLevel, logMessageSupplier);
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
    public GameEvent processEffect(EntityEffect effect, boolean reverse) {
        // TODO make effects applicable here
        return null;
    }

    @Override
    public String getName() {
        // TODO: do dungeons need names?
        return "Ibaif";
    }

    @Override
    public String getStartTag() {
        return "<Dungeon>";
    }

    @Override
    public String getEndTag() {
        return "</Dungeon>";
    }

    @Override
    public String printDescription() {
        return String.format("This Dungeon is called %s and it has %d rooms!", this.getColorTaggedName(),
                this.mapping.size());
    }

    @Override
    public SeeEvent produceMessage() {
        return SeeEvent.getBuilder().setExaminable(this).Build();
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Dungeon [name=").append(this.getName()).append(", startingRoom=").append(startingRoom)
                .append(", numRooms=").append(this.mapping != null ? this.mapping.size() : 0).append("]");
        return builder.toString();
    }

}
