package com.lhf.game.map;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.EntityEffect;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.IMonster;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.INonPlayerCharacter.AbstractNPCBuilder;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.item.Item;
import com.lhf.game.item.ItemNoOpVisitor;
import com.lhf.game.item.ItemVisitor;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.map.Area.AreaBuilder.PostBuildRoomOperations;
import com.lhf.game.map.SubArea.SubAreaBuilder;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.events.CreatureDiedEvent;
import com.lhf.messages.events.RoomAffectedEvent;
import com.lhf.messages.events.RoomEnteredEvent;
import com.lhf.messages.events.RoomExitedEvent;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.server.client.user.UserID;

public class Room implements Area {
    private final GameEventProcessorID gameEventProcessorID = new GameEventProcessorID();
    private final UUID uuid = gameEventProcessorID.getUuid();
    protected final Logger logger;
    final List<Item> items;
    private final String description;
    private final String name;
    private final NavigableSet<SubArea> subAreas;
    private final Set<ICreature> allCreatures;
    private final transient Land land;
    private final transient TreeSet<RoomEffect> effects;
    private final transient ItemVisitor itemAdditionVisitor = new ItemNoOpVisitor() {
        @Override
        public void visit(com.lhf.game.item.InteractObject interactObject) {
            if (interactObject != null) {
                interactObject.setArea(Room.this);
            }
        };
    };
    private final transient ItemVisitor itemRemovalVisitor = new ItemNoOpVisitor() {
        @Override
        public void visit(com.lhf.game.item.InteractObject interactObject) {
            if (interactObject != null) {
                interactObject.setArea(null);
            }
        };
    };

    private transient Map<AMessageType, CommandHandler> commands;
    private transient CommandChainHandler successor;

    public static class RoomBuilder implements Area.AreaBuilder {
        private final transient Logger logger;
        private final transient AreaBuilderID id;
        private String name;
        private String description;
        private List<Item> items;
        private Set<INonPlayerCharacter.AbstractNPCBuilder<?, ?>> npcsToBuild;
        private Set<SubAreaBuilder<?, ?>> subAreasToBuild;

        private RoomBuilder() {
            this.logger = Logger.getLogger(this.getClass().getName());
            this.id = new AreaBuilderID();
            this.name = null;
            this.description = "An area that Creatures and Items can be in";
            this.items = new ArrayList<>();
            this.npcsToBuild = new HashSet<>();
            this.subAreasToBuild = new HashSet<>();
        }

        public static RoomBuilder getInstance() {
            return new RoomBuilder();
        }

        @Override
        public AreaBuilderID getAreaBuilderID() {
            return this.id;
        }

        public RoomBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public RoomBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public RoomBuilder addItem(Item item) {
            if (this.items == null) {
                this.items = new ArrayList<>();
            }
            if (item != null) {
                this.items.add(item);
            }
            return this;
        }

        public RoomBuilder addNPCBuilder(INonPlayerCharacter.AbstractNPCBuilder<?, ?> builder) {
            if (this.npcsToBuild == null) {
                this.npcsToBuild = new HashSet<>();
            }
            if (builder != null) {
                this.npcsToBuild.add(builder);
            }
            return this;
        }

        @Override
        public Collection<AbstractNPCBuilder<?, ?>> getNPCsToBuild() {
            return this.npcsToBuild;
        }

        public RoomBuilder addSubAreaBuilder(SubAreaBuilder<?, ?> builder) {
            if (this.subAreasToBuild == null) {
                this.subAreasToBuild = new HashSet<>();
            }
            if (builder != null) {
                this.subAreasToBuild.add(builder);
            }
            return this;
        }

        @Override
        public Collection<SubAreaBuilder<?, ?>> getSubAreasToBuild() {
            return this.subAreasToBuild;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public Collection<Item> getItems() {
            return this.items;
        }

        @Override
        public String getName() {
            return this.name != null ? this.name : "Room " + UUID.randomUUID().toString();
        }

        protected Set<INonPlayerCharacter> quickBuildCreatures(AIRunner aiRunner, Room successor) {
            Collection<AbstractNPCBuilder<?, ?>> toBuild = this.getNPCsToBuild();
            if (toBuild == null) {
                return Set.of();
            }
            TreeSet<INonPlayerCharacter> built = new TreeSet<>();
            for (final AbstractNPCBuilder<?, ?> builder : toBuild) {
                if (builder == null) {
                    continue;
                }
                built.add(builder.quickBuild(aiRunner, successor));
            }
            return Collections.unmodifiableSet(built);
        }

        @Override
        public Room quickBuild(CommandChainHandler successor, Land land, AIRunner aiRunner) {
            this.logger.log(Level.INFO, () -> String.format("QUICK Building room '%s'", this.name));
            return Room.quickBuilder(this, () -> land, () -> successor, () -> (room) -> {
                final Set<INonPlayerCharacter> creaturesBuilt = this.quickBuildCreatures(aiRunner, room);
                room.addCreatures(creaturesBuilt, true);
                for (final SubAreaBuilder<?, ?> subAreaBuilder : this.getSubAreasToBuild()) {
                    room.addSubArea(subAreaBuilder);
                }
            });
        }

        protected Set<INonPlayerCharacter> buildCreatures(
                AIRunner aiRunner, Room successor, StatblockManager statblockManager,
                ConversationManager conversationManager) throws FileNotFoundException {
            Collection<AbstractNPCBuilder<?, ?>> toBuild = this.getNPCsToBuild();
            if (toBuild == null) {
                return Set.of();
            }
            TreeSet<INonPlayerCharacter> built = new TreeSet<>();
            for (final AbstractNPCBuilder<?, ?> builder : toBuild) {
                if (builder == null) {
                    continue;
                }
                built.add(builder.build(aiRunner, successor, statblockManager, conversationManager));
            }
            return Collections.unmodifiableSet(built);
        }

        @Override
        public Room build(CommandChainHandler successor, Land land, AIRunner aiRunner,
                StatblockManager statblockManager, ConversationManager conversationManager)
                throws FileNotFoundException {
            this.logger.log(Level.INFO, () -> String.format("Building room '%s'", this.name));
            return Room.fromBuilder(this, () -> land, () -> successor, () -> (room) -> {
                final Set<INonPlayerCharacter> creaturesBuilt = this.buildCreatures(aiRunner, room, statblockManager,
                        conversationManager);
                room.addCreatures(creaturesBuilt, false);
                for (final SubAreaBuilder<?, ?> subAreaBuilder : this.getSubAreasToBuild()) {
                    room.addSubArea(subAreaBuilder);
                }
            });
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof RoomBuilder))
                return false;
            RoomBuilder other = (RoomBuilder) obj;
            return Objects.equals(id, other.id);
        }

    }

    static Room fromBuilder(RoomBuilder builder, Supplier<Land> landSupplier,
            Supplier<CommandChainHandler> successorSupplier,
            Supplier<PostBuildRoomOperations<? super Room>> postOperations)
            throws FileNotFoundException {
        Room created = new Room(builder, landSupplier, successorSupplier);
        if (postOperations != null) {
            PostBuildRoomOperations<? super Room> postOp = postOperations.get();
            if (postOp != null) {
                postOp.accept(created);
            }
        }
        return created;
    }

    static Room quickBuilder(RoomBuilder builder, Supplier<Land> landSupplier,
            Supplier<CommandChainHandler> successorSupplier, Supplier<Consumer<? super Room>> postOperations) {
        Room created = new Room(builder, landSupplier, successorSupplier);
        if (postOperations != null) {
            Consumer<? super Room> postOp = postOperations.get();
            if (postOp != null) {
                postOp.accept(created);
            }
        }
        return created;
    }

    Room(RoomBuilder builder, Supplier<Land> landSupplier,
            Supplier<CommandChainHandler> successorSupplier) {
        this.name = builder.getName();
        this.logger = Logger.getLogger(this.getClass().getName() + "."
                + (this.name != null && !this.name.isBlank() ? this.name.replaceAll("\\W", "_")
                        : this.uuid.toString()));
        this.description = builder.getDescription() != null ? builder.getDescription() : builder.getName();
        this.items = new ArrayList<>(builder.getItems());
        for (final Item item : this.items) {
            item.acceptItemVisitor(itemAdditionVisitor);
        }
        this.allCreatures = new TreeSet<>();
        this.land = landSupplier.get();
        this.successor = successorSupplier.get();
        this.effects = new TreeSet<>();
        this.subAreas = new TreeSet<>();
        this.commands = this.buildCommands();
    }

    protected Map<AMessageType, CommandHandler> buildCommands() {
        Map<AMessageType, CommandHandler> cmds = new EnumMap<>(Area.AreaCommandHandler.areaCommandHandlers);
        return cmds;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getStartTag() {
        return "<room>";
    }

    @Override
    public String getEndTag() {
        return "</room>";
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public Land getLand() {
        return this.land;
    }

    @Override
    public Set<ICreature> getCreatures() {
        return Collections.unmodifiableSet(this.allCreatures);
    }

    public void addCreatures(Set<? extends ICreature> creaturesToAdd, boolean silent) {
        StringJoiner sj = new StringJoiner(", ", "Added creatures: ", "").setEmptyValue("No creatures added");
        if (creaturesToAdd != null) {
            creaturesToAdd.stream().filter(c -> c != null).forEach(c -> {
                c.setSuccessor(this);
                if (this.allCreatures.add(c)) {
                    sj.add(c.getName());
                    if (!silent) {
                        ICreature.eventAccepter.accept(c, this.produceMessage());
                        this.announce(RoomEnteredEvent.getBuilder().setNewbie(c).setBroacast().Build(), c);
                    }
                    for (final SubArea subArea : this.subAreas) {
                        subArea.onAreaEntry(c);
                    }
                }
            });
        }
        this.log(Level.INFO, sj.toString());
    }

    @Override
    public boolean addCreature(ICreature c) {
        c.setSuccessor(this);
        if (this.allCreatures.add(c)) {
            this.logger.log(Level.FINER, () -> String.format("%s entered the room", c.getName()));
            ICreature.eventAccepter.accept(c, this.produceMessage());
            this.announce(RoomEnteredEvent.getBuilder().setNewbie(c).setBroacast().Build(), c);
            for (final SubArea subArea : this.subAreas) {
                subArea.onAreaEntry(c);
            }
            return true;
        }
        return false;
    }

    @Override
    public Optional<ICreature> removeCreature(String name) {
        Optional<ICreature> found = this.getCreature(name);
        if (found.isPresent()) {
            this.removeCreature(found.get());
        }
        return found;
    }

    @Override
    public boolean removeCreature(ICreature c) {
        for (final SubArea subArea : this.getSubAreas()) {
            subArea.removeCreature(c);
        }
        if (this.allCreatures.remove(c)) {
            c.setSuccessor(this.getSuccessor());
            return true;
        }
        return false;
    }

    @Override
    public boolean removeCreature(ICreature c, Directions dir) {
        boolean removed = removeCreature(c);
        if (removed) {
            this.announce(RoomExitedEvent.getBuilder().setLeaveTaker(c).setWhichWay(dir).Build());
        }
        return removed;
    }

    @Override
    public boolean addPlayer(Player p) {
        return this.addCreature(p);
    }

    @Override
    public Optional<Player> removePlayer(String name) {
        Optional<Player> found = this.getPlayer(name);
        if (found.isPresent()) {
            this.removeCreature(found.get());
        }
        return found;
    }

    @Override
    public Optional<Player> removePlayer(UserID id) {
        Optional<Player> toRemove = getPlayer(id);
        if (toRemove.isPresent()) {
            this.removeCreature(toRemove.get());
        }
        return toRemove;
    }

    @Override
    public boolean removePlayer(Player player) {
        return this.removeCreature(player);
    }

    @Override
    public boolean onCreatureDeath(ICreature creature) {
        boolean removed = this.removeCreature(creature);
        if (removed) {
            this.logger.log(Level.FINER, () -> String.format("The creature '%s' has died", creature.getName()));
            CreatureDiedEvent deathEvent = CreatureDiedEvent.getBuilder().setDearlyDeparted(creature).Build();
            ICreature.eventAccepter.accept(creature, deathEvent); // tell the creature it has died
            Area.eventAccepter.accept(this, deathEvent); // and then tell everyone else
            Corpse corpse = ICreature.die(creature);
            this.addItem(corpse);
        }
        removed = this.land.onCreatureDeath(creature) || removed;

        return removed;
    }

    @Override
    public Collection<Item> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    @Override
    public boolean addItem(Item obj) {
        if (obj == null) {
            return false;
        }

        if (items.add(obj)) {
            obj.acceptItemVisitor(itemAdditionVisitor);
            return true;
        }
        return false;
    }

    /**
     * Checks to see if we have the exact name of the item.
     */
    @Override
    public boolean hasItem(String itemName) {
        return this.items.stream().anyMatch(item -> item != null && item.checkName(itemName));
    }

    @Override
    public Optional<Item> removeItem(String name) {
        for (Iterator<Item> iterator = this.items.iterator(); iterator.hasNext();) {
            Item item = iterator.next();
            if (item != null && item.checkName(name)) {
                iterator.remove();
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean removeItem(Item item) {
        if (this.items.remove(item)) {
            item.acceptItemVisitor(itemRemovalVisitor);
        }
        return false;
    }

    @Override
    public Iterator<? extends Item> itemIterator() {
        return this.items.iterator();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Room [name=").append(name).append(", description=").append(description).append(", uuid=")
                .append(uuid).append("]");
        return builder.toString();
    }

    @Override
    public String printDescription() {
        return "<description>" + this.description + "</description>";
    }

    @Override
    public SeeEvent produceMessage(boolean seeInvisible, boolean seeDirections) {
        SeeEvent.Builder seeOutMessage = (SeeEvent.Builder) Area.super.produceMessage(seeInvisible,
                seeDirections).copyBuilder();

        for (final SubArea subArea : this.subAreas) {
            seeOutMessage.addExtraInfo(subArea.printDescription());
        }
        return seeOutMessage.Build();
    }

    @Override
    public boolean isCorrectEffectType(EntityEffect effect) {
        return effect != null && effect instanceof RoomEffect;
    }

    @Override
    public RoomAffectedEvent processEffect(EntityEffect effect, boolean reverse) {
        if (!this.isCorrectEffectType(effect)) {
            return null;
        }
        this.logger.log(Level.FINER, () -> String.format("Room processing effect '%s'", effect.getName()));
        RoomEffect roomEffect = (RoomEffect) effect;
        INonPlayerCharacter summonedNPC = roomEffect.getQuickSummonedNPC(this);
        if (summonedNPC != null) {
            this.logger.log(Level.INFO, () -> String.format("Summoned npc %s", summonedNPC.getName()));
            this.addCreature(summonedNPC);
        }
        IMonster summonedMonster = roomEffect.getQuickSummonedMonster(this);
        if (summonedMonster != null) {
            this.logger.log(Level.INFO, () -> String.format("Summoned monster %s", summonedMonster.getName()));
            this.addCreature(summonedMonster);
        }
        return RoomAffectedEvent.getBuilder().setRoom(this).setEffect(roomEffect).Build();
    }

    @Override
    public NavigableSet<RoomEffect> getMutableEffects() {
        return this.effects;
    }

    @Override
    public NavigableSet<SubArea> getSubAreas() {
        return Collections.unmodifiableNavigableSet(this.subAreas);
    }

    @Override
    public boolean addSubArea(SubAreaBuilder<?, ?> builder) {
        if (builder != null && !this.hasSubAreaSort(builder.getSubAreaSort())) {
            SubArea built = builder.build(this);
            boolean done = this.subAreas.add(built);
            if (done && built != null && !builder.isQueryOnBuild()) {
                for (final CreatureFilterQuery query : builder.getCreatureQueries()) {
                    for (ICreature creature : this.filterCreatures(query)) {
                        built.addCreature(creature);
                    }
                }
            }
            return done;
        }
        return false;
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
    public GameEventProcessorID getEventProcessorID() {
        return this.gameEventProcessorID;
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
    public CommandContext addSelfToContext(CommandContext ctx) {
        if (ctx.getArea() == null) {
            ctx.setArea(this);
        }
        return ctx;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Room)) {
            return false;
        }
        Room other = (Room) obj;
        return Objects.equals(name, other.name) && Objects.equals(uuid, other.uuid);
    }

}
