package com.lhf.game.map;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.EntityEffect;
import com.lhf.game.creature.CreatureFactory;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.Player.PlayerBuildInfo;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.map.Area.AreaBuilder;
import com.lhf.game.map.Area.AreaBuilder.AreaBuilderID;
import com.lhf.game.map.Atlas.AtlasMappingItem;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.events.CreatureSpawnedEvent;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.PlayerReincarnatedEvent;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.server.client.user.UserID;

public class Dungeon implements Land {

    public static class DungeonBuilder implements Land.LandBuilder {
        protected final String className;
        private final transient Logger logger;
        private final LandBuilderID id;
        private String name;
        private AreaBuilder startingRoom = null;
        private AreaBuilderAtlas atlas = null;

        public static DungeonBuilder newInstance() {
            return new DungeonBuilder();
        }

        private DungeonBuilder() {
            this.className = this.getClass().getName();
            this.logger = Logger.getLogger(this.getClass().getName());
            this.id = new LandBuilderID();
            this.name = null;
            this.atlas = new AreaBuilderAtlas();
        }

        public LandBuilderID getLandBuilderID() {
            return this.id;
        }

        public DungeonBuilder setName(String dungeonName) {
            this.name = dungeonName;
            return this;
        }

        public String getName() {
            return this.name != null ? this.name : "Ibaif " + UUID.randomUUID().toString();
        }

        public DungeonBuilder addStartingRoom(AreaBuilder startingRoom) {
            if (startingRoom == null) {
                throw new IllegalArgumentException("Cannot add null starting room");
            }
            this.atlas.addMember(startingRoom);
            this.startingRoom = startingRoom;

            return this;
        }

        public DungeonBuilder connectRoom(AreaBuilder first, Directions toSecond,
                AreaBuilder second, Doorway type) {
            if (this.atlas == null || this.startingRoom == null) {
                throw new IllegalStateException("Cannot connect a room without first specifying a starting room!");
            }
            this.atlas.connect(first, toSecond, second, type);
            return this;
        }

        public DungeonBuilder connectRoom(AreaBuilder first, Directions toSecond, AreaBuilder second) {
            return this.connectRoom(first, toSecond, second, new Doorway());
        }

        public DungeonBuilder connectRoomOneWay(AreaBuilder first, Directions toSecond,
                AreaBuilder second) {
            if (this.atlas == null || this.startingRoom == null) {
                throw new IllegalStateException("Cannot connect a room without first specifying a starting room!");
            }
            this.atlas.connectOneWay(first, toSecond, second, new OneWayDoorway(toSecond));
            return this;
        }

        @Override
        public Dungeon quickBuild(CommandChainHandler successor, AIRunner aiRunner) {
            return build(successor, aiRunner, null, null, true, true);
        }

        @Override
        public Dungeon build(CommandChainHandler successor, AIRunner aiRunner, StatblockManager statblockManager,
                ConversationManager conversationManager,
                boolean fallbackNoConversation,
                boolean fallbackDefaultStatblock) {
            this.logger.entering(this.getClass().getName(), "build()");
            return Dungeon.fromBuilder(this, () -> successor, () -> (dungeon) -> {
                Map<AreaBuilderID, UUID> translation = this.translateAtlas(dungeon, aiRunner, statblockManager,
                        conversationManager, fallbackNoConversation, fallbackDefaultStatblock);
                if (translation != null && this.startingRoom != null) {
                    AreaBuilderID builderID = this.startingRoom.getAreaBuilderID();
                    dungeon.setStartingAreaUUID(translation.get(builderID));
                }
            });
        }

        public static Dungeon buildDynamicDungeon(int seed, AIRunner aiRunner,
                ConversationManager convoLoader, StatblockManager statblockLoader) {

            return null;
        }

        @Override
        public AreaBuilder getStartingAreaBuilder() {
            return this.startingRoom;
        }

        @Override
        public AreaBuilderAtlas getAtlas() {
            return this.atlas;
        }

        public String toMermaid(boolean fence) {
            return "DungeonBuilder\r\n" + this.atlas.toMermaid(fence);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof DungeonBuilder))
                return false;
            DungeonBuilder other = (DungeonBuilder) obj;
            return Objects.equals(id, other.id);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(this.getClass().getSimpleName()).append(" [id=").append(id).append(", name=")
                    .append(name).append(", startingRoom=").append(startingRoom).append(", atlas=").append(atlas)
                    .append("]");
            return builder.toString();
        }

    }

    private final Land.AreaAtlas atlas;
    private UUID startingAreaUUID;
    private transient CommandChainHandler successor;
    private Map<AMessageType, CommandHandler> commands;
    private transient TreeSet<DungeonEffect> effects;
    private transient final Logger logger;
    private final GameEventProcessorID gameEventProcessorID;
    private final String name;

    static Dungeon fromBuilder(Land.LandBuilder builder,
            Supplier<CommandChainHandler> successorSupplier, Supplier<Consumer<Dungeon>> postOperation) {
        Dungeon built = new Dungeon(builder, successorSupplier);
        if (postOperation != null) {
            Consumer<Dungeon> postOp = postOperation.get();
            if (postOp != null) {
                postOp.accept(built);
            }
        }
        return built;
    }

    Dungeon(Land.LandBuilder builder, Supplier<CommandChainHandler> successorSupplier) {
        this.logger = Logger.getLogger(String.format("%s.%s", this.getClass().getName(), this.getName()));
        this.gameEventProcessorID = new GameEventProcessorID();
        this.atlas = new AreaAtlas();
        this.startingAreaUUID = null;
        this.name = builder.getName();
        this.successor = successorSupplier.get();
        this.commands = this.buildCommands();
        this.effects = new TreeSet<>();
    }

    @Override
    public Land.AreaAtlas getAtlas() {
        return this.atlas;
    }

    @Override
    public void setStartingAreaUUID(UUID areaID) {
        this.startingAreaUUID = areaID;
    }

    @Override
    public UUID getStartingAreaUUID() {
        return this.startingAreaUUID;
    }

    private Map<AMessageType, CommandHandler> buildCommands() {
        Map<AMessageType, CommandHandler> cmds = new EnumMap<>(Land.LandCommandHandler.landCommandHandlers);
        return cmds;
    }

    @Override
    public GameEventProcessorID getEventProcessorID() {
        return this.gameEventProcessorID;
    }

    @Override
    public boolean addPlayer(Player player) {
        Area startingRoom = this.getStartingArea();
        startingRoom
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
        Optional<Player> removed = room.removePlayer(id);
        if (removed.isPresent()) {
            removed.get().setSuccessor(this.getSuccessor());
        }
        return removed;
    }

    @Override
    public boolean removePlayer(Player player) {
        return this.removePlayer(player.getId()).isPresent();
    }

    @Override
    public Optional<Player> getPlayer(UserID id) {
        for (final Area area : this.atlas.getAtlasMembers()) {
            Optional<Player> found = area.getPlayer(id);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean addCreature(ICreature creature) {
        Area startingRoom = this.getStartingArea();
        startingRoom
                .announce(CreatureSpawnedEvent.getBuilder().setCreatureName(creature.getColorTaggedName()).setBroacast()
                        .Build());
        creature.setSuccessor(this);
        return startingRoom.addCreature(creature);
    }

    public boolean addCreature(ICreature creature, UUID roomUUID) {
        AtlasMappingItem<Area, UUID> areaInfo = this.atlas.getAtlasMappingItem(roomUUID);
        if (areaInfo != null && areaInfo.getAtlasMember() != null) {
            areaInfo.getAtlasMember()
                    .announce(CreatureSpawnedEvent.getBuilder().setCreatureName(creature.getColorTaggedName()).Build());
            creature.setSuccessor(this);
            return areaInfo.getAtlasMember().addCreature(creature);
        }
        return false;
    }

    @Override
    public Optional<ICreature> removeCreature(String name) {
        Area room = this.getCreatureArea(name);
        if (room == null) {
            return Optional.empty();
        }
        Optional<ICreature> removed = room.removeCreature(name);
        if (removed.isPresent()) {
            removed.get().setSuccessor(this.getSuccessor());
        }
        return removed;
    }

    @Override
    public boolean removeCreature(ICreature creature) {
        Area room = this.getCreatureArea(creature);
        if (room == null) {
            return false;
        }
        if (room.removeCreature(creature)) {
            creature.setSuccessor(this.getSuccessor());
            return true;
        }
        return false;
    }

    @Override
    public Optional<Player> removePlayer(String name) {
        for (final Area area : this.atlas.getAtlasMembers()) {
            Optional<Player> found = area.removePlayer(name);
            if (found.isPresent()) {
                found.get().setSuccessor(this.getSuccessor());
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

        if (creature != null && creature instanceof Player oldLife) {
            CreatureFactory factory = new CreatureFactory();
            PlayerBuildInfo nextLifeInfo = Player.PlayerBuildInfo.getInstance(oldLife.getUser())
                    .setVocation(oldLife.getVocation().resetLevel());
            factory.visit(nextLifeInfo);
            Player nextLife = factory.getBuiltCreatures().getPlayers().first();
            nextLife.setSuccessor(this.getStartingArea());
            oldLife.disconnectController(); // events will now not go anywhere
            ICreature.eventAccepter.accept(nextLife,
                    PlayerReincarnatedEvent.getBuilder().setTaggedName(creature).setNotBroadcast().Build());
            // ICreature.eventAccepter.accept(nextLife,
            // SeeEvent.getBuilder().setExaminable(startingRoom).Build());
            this.addPlayer(nextLife);
        }
        return removed;
    }

    boolean connectRoom(Doorway type, Room toAdd, Directions toExistingRoom, Room existing) {
        try {
            this.atlas.connect(existing, toExistingRoom.opposite(), toAdd, type);
            return true;
        } catch (IllegalArgumentException | IllegalStateException e) {
            this.log(Level.WARNING, e.toString());
            return false;
        }
    }

    boolean connectRoomExclusiveOneWay(Room secretRoom, Directions toExistingRoom, Room existing) {
        try {
            this.atlas.connectOneWay(existing, toExistingRoom, secretRoom, new OneWayDoorway(toExistingRoom));
            return true;
        } catch (IllegalArgumentException | IllegalStateException e) {
            this.log(Level.WARNING, e.toString());
            return false;
        }
    }

    public void announceToAllInRoom(Room room, GameEvent event, GameEventProcessor... deafened) {
        if (room == null) {
            Area startingRoom = this.getStartingArea();
            if (startingRoom != null) {
                startingRoom.announce(event, deafened);
            }
            return;
        }
        room.announce(event, deafened);
    }

    @Override
    public void setSuccessor(CommandChainHandler successor) {
        this.successor = successor;
    }

    @Override
    public CommandChainHandler getSuccessor() {
        return this.successor;
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        ctx.setLand(this);
        return ctx;
    }

    @Override
    public Map<AMessageType, CommandHandler> getCommands(CommandContext ctx) {
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
        return this.name;
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
                this.atlas.size());
    }

    @Override
    public SeeEvent produceMessage() {
        return SeeEvent.getBuilder().setExaminable(this).Build();
    }

    public String toMermaid(boolean fence) {
        return this.getName() + "\r\n" + this.atlas.toMermaid(fence);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Dungeon [name=").append(this.getName()).append(", startingRoom=")
                .append(this.atlas.getFirstMember())
                .append(", numRooms=").append(this.atlas != null ? this.atlas.size() : 0).append("]");
        return builder.toString();
    }

}
