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
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

import com.lhf.Examinable;
import com.lhf.game.EntityEffect;
import com.lhf.game.ItemContainer;
import com.lhf.game.LockableItemContainer;
import com.lhf.game.battle.BattleManager;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.IMonster;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.INonPlayerCharacter.AbstractNPCBuilder;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.item.InteractObject;
import com.lhf.game.item.Item;
import com.lhf.game.item.Takeable;
import com.lhf.game.item.Usable;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.map.Area.AreaBuilder.PostBuildRoomOperations;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.events.BadMessageEvent;
import com.lhf.messages.events.BadMessageEvent.BadMessageType;
import com.lhf.messages.events.BadSpeakingTargetEvent;
import com.lhf.messages.events.BadTargetSelectedEvent;
import com.lhf.messages.events.BadTargetSelectedEvent.BadTargetOption;
import com.lhf.messages.events.CreatureDiedEvent;
import com.lhf.messages.events.ItemDroppedEvent;
import com.lhf.messages.events.ItemDroppedEvent.DropType;
import com.lhf.messages.events.ItemInteractionEvent;
import com.lhf.messages.events.ItemInteractionEvent.InteractOutMessageType;
import com.lhf.messages.events.ItemNotPossessedEvent;
import com.lhf.messages.events.ItemTakenEvent;
import com.lhf.messages.events.ItemTakenEvent.TakeOutType;
import com.lhf.messages.events.ItemUsedEvent;
import com.lhf.messages.events.ItemUsedEvent.UseOutMessageOption;
import com.lhf.messages.events.RoomAffectedEvent;
import com.lhf.messages.events.RoomEnteredEvent;
import com.lhf.messages.events.RoomExitedEvent;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.events.SpeakingEvent;
import com.lhf.messages.in.DropMessage;
import com.lhf.messages.in.InteractMessage;
import com.lhf.messages.in.SayMessage;
import com.lhf.messages.in.SeeMessage;
import com.lhf.messages.in.TakeMessage;
import com.lhf.messages.in.UseMessage;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.client.user.UserID;

public class Room implements Area {
    private final GameEventProcessorID gameEventProcessorID = new GameEventProcessorID();
    private final UUID uuid = gameEventProcessorID.getUuid();
    protected final Logger logger;
    private final List<Item> items;
    private transient Long takeableCount;
    private transient Long interactableCount;
    private final String description;
    private final String name;
    private final BattleManager battleManager;
    private final Set<ICreature> allCreatures;
    private final transient Land land;
    private final transient TreeSet<RoomEffect> effects;

    private transient Map<CommandMessage, CommandHandler> commands;
    private transient CommandChainHandler successor;

    public static class RoomBuilder implements Area.AreaBuilder {
        private final transient Logger logger;
        private final AreaBuilderID id;
        private String name;
        private String description;
        private List<Item> items;
        private Set<INonPlayerCharacter.AbstractNPCBuilder<?, ?>> npcsToBuild;
        private BattleManager.Builder battleManagerBuilder;

        private RoomBuilder() {
            this.logger = Logger.getLogger(this.getClass().getName());
            this.id = new AreaBuilderID();
            this.name = null;
            this.description = "An area that Creatures and Items can be in";
            this.items = new ArrayList<>();
            this.npcsToBuild = new HashSet<>();
            this.battleManagerBuilder = BattleManager.Builder.getInstance();
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
        this.allCreatures = new TreeSet<>();
        this.land = landSupplier.get();
        this.successor = successorSupplier.get();
        this.effects = new TreeSet<>();
        this.battleManager = builder.battleManagerBuilder.Build(this);
        this.commands = this.buildCommands();
    }

    protected Map<CommandMessage, CommandHandler> buildCommands() {
        Map<CommandMessage, CommandHandler> cmds = new EnumMap<>(CommandMessage.class);
        cmds.put(CommandMessage.SAY, new SayHandler());
        cmds.put(CommandMessage.SEE, new SeeHandler());
        cmds.put(CommandMessage.DROP, new DropHandler());
        cmds.put(CommandMessage.USE, new UseHandler());
        cmds.put(CommandMessage.CAST, new CastHandler());
        cmds.put(CommandMessage.ATTACK, new AttackHandler());
        cmds.put(CommandMessage.INTERACT, new InteractHandler());
        cmds.put(CommandMessage.TAKE, new TakeHandler());
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
                    if (this.battleManager.isBattleOngoing("Room.addCreatures(Set<ICreature>)")
                            && !CreatureFaction.NPC.equals(c.getFaction())) {
                        this.battleManager.addCreature(c);
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
            if (this.battleManager.isBattleOngoing("Room.addCreature()")
                    && !CreatureFaction.NPC.equals(c.getFaction())) {
                this.battleManager.addCreature(c);
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
        if (this.battleManager.hasCreature(c)) {
            this.battleManager.removeCreature(c);
            c.setInBattle(false);
            c.setSuccessor(this.getSuccessor());
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
        long takeCount = this.getTakeableCount();
        long interactCount = this.getInteractableCount();
        items.add(obj);
        if (obj instanceof InteractObject) {
            this.interactableCount = interactCount - 1;
        }
        if (obj instanceof Takeable) {
            this.takeableCount = takeCount - 1;
        }
        return true;
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
        long takeCount = this.getTakeableCount();
        long interactCount = this.getInteractableCount();
        for (Iterator<Item> iterator = this.items.iterator(); iterator.hasNext();) {
            Item item = iterator.next();
            if (item != null && item.checkName(name)) {
                iterator.remove();
                if (item instanceof InteractObject) {
                    this.interactableCount = interactCount - 1;
                }
                if (item instanceof Takeable) {
                    this.takeableCount = takeCount - 1;
                }
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean removeItem(Item item) {
        long takeCount = this.getTakeableCount();
        long interactCount = this.getInteractableCount();
        boolean did = this.items.remove(item);
        if (did) {
            if (item instanceof InteractObject) {
                this.interactableCount = interactCount - 1;
            }
            if (item instanceof Takeable) {
                this.takeableCount = takeCount - 1;
            }
        }
        return did;
    }

    @Override
    public Iterator<? extends Item> itemIterator() {
        return this.items.iterator();
    }

    /**
     * Pull-through cache of TakeableCount
     * 
     * @return
     */
    public long getTakeableCount() {
        if (this.takeableCount == null) {
            this.takeableCount = this.items.stream().filter(item -> item != null && item instanceof Takeable).count();
        }
        return this.takeableCount;
    }

    /**
     * Pull-through cache of InteractableCount
     * 
     * @return
     */
    public long getInteractableCount() {
        if (this.interactableCount == null) {
            this.interactableCount = this.items.stream().filter(item -> item != null && item instanceof InteractObject)
                    .count();
        }
        return this.interactableCount;
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

        if (this.battleManager.isBattleOngoing("Room.produceMessage()")) {
            seeOutMessage.addExtraInfo("There is a battle going on!");
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

    public String getBattleInfo() {
        return battleManager.produceMessage().print();
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
    public CommandContext addSelfToContext(CommandContext ctx) {
        if (ctx.getRoom() == null) {
            ctx.setRoom(this);
        }
        return ctx;
    }

    public interface RoomCommandHandler extends ICreature.CreatureCommandHandler {
        final static Predicate<CommandContext> defaultRoomPredicate = ICreature.CreatureCommandHandler.defaultCreaturePredicate
                .and(ctx -> ctx.getRoom() != null);
        final static Predicate<CommandContext> defaultNoBattlePredicate = RoomCommandHandler.defaultRoomPredicate
                .and(ctx -> !ctx.getCreature().isInBattle());
        final static String inBattleString = "You appear to be in a fight, so you cannot do that.";
    }

    protected class AttackHandler implements RoomCommandHandler {
        private final static String helpString = new StringJoiner(" ")
                .add("\"attack [name]\"").add("Attacks a creature").add("\r\n")
                .add("\"attack [name] with [weapon]\"").add("Attack the named creature with a weapon that you have.")
                .add("In the unlikely event that either the creature or the weapon's name contains 'with', enclose the name in quotation marks.")
                .toString();
        private final static Predicate<CommandContext> enabledPredicate = AttackHandler.defaultRoomPredicate
                .and(ctx -> {
                    Room room = ctx.getRoom();
                    if (room.battleManager == null) {
                        room.logger.warning(() -> String.format("No battle manager for room: %s", room.getName()));
                        return false;
                    }
                    return room.getCreatures().size() > 1;
                });

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.ATTACK;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(AttackHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return AttackHandler.enabledPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd == null || cmd.getType() != CommandMessage.ATTACK) {
                return ctx.failhandle();
            }
            ctx = Room.this.addSelfToContext(ctx);
            if (ctx.getCreature() == null) {
                ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                        .setHelps(ctx.getHelps()).setCommand(cmd).Build());
                return ctx.handled();
            }
            return Room.this.battleManager.handleChain(ctx, cmd);
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return Room.this;
        }

    }

    protected class CastHandler implements RoomCommandHandler {

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.CAST;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.empty();
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return CastHandler.defaultRoomPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            ctx.setBattleManager(Room.this.battleManager);
            if (ctx.getCreature() == null) {
                ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                        .setHelps(ctx.getHelps()).setCommand(cmd).Build());
                return ctx.handled();
            }
            return ctx.failhandle(); // let a successor (ThirdPower) handle it
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return Room.this;
        }

    }

    protected class TakeHandler implements RoomCommandHandler {
        private final static Predicate<CommandContext> enabledPredicate = InteractHandler.defaultNoBattlePredicate
                .and(ctx -> ctx.getRoom().getTakeableCount() > 0);
        private final static String helpString = new StringJoiner(" ").add("\"take [item]\"")
                .add("Take an item from the room and add it to your inventory.\n")
                .add("\"take [item] from \"[someone]'s corpse\"")
                .add("Take an item from a container of some kind, just double-quote the container name")
                .toString();

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.TAKE;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            if (ctx == null || ctx.getCreature() == null) {
                return Optional.empty();
            }
            return Optional
                    .of(ctx.getCreature().isInBattle() ? TakeHandler.inBattleString : TakeHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return TakeHandler.enabledPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.TAKE && cmd instanceof TakeMessage tMessage) {
                if (ctx.getCreature() == null) {
                    ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                            .setHelps(ctx.getHelps()).setCommand(tMessage).Build());
                    return ctx.handled();
                }

                ItemTakenEvent.Builder takeOutMessage = ItemTakenEvent.getBuilder();

                ItemContainer container = Room.this;
                takeOutMessage.setSource(Room.this);
                Optional<String> containerName = tMessage.fromContainer();
                if (containerName.isPresent()) {
                    takeOutMessage.setSource(containerName.orElse(null));
                    Optional<ItemContainer> foundContainer = Room.this.items.stream()
                            .filter(item -> item != null && item instanceof ItemContainer
                                    && item.checkName(containerName.get().replaceAll("^\"|\"$", "")))
                            .map(item -> (ItemContainer) item).findAny();
                    if (foundContainer.isEmpty()) {
                        ctx.receive(takeOutMessage.setSubType(TakeOutType.BAD_CONTAINER).Build());
                        return ctx.handled();
                    }
                    if (foundContainer.get() instanceof LockableItemContainer liCon) {
                        if (!liCon.canAccess(ctx.getCreature())) {
                            ctx.receive(takeOutMessage.setSubType(TakeOutType.LOCKED_CONTAINER).Build());
                            return ctx.handled();
                        }
                        container = liCon.getBypass();
                    } else {
                        container = foundContainer.get();
                    }
                }

                for (String thing : tMessage.getDirects()) {
                    takeOutMessage.setAttemptedName(thing);
                    if (thing.length() < 3) {
                        ctx.receive(takeOutMessage.setSubType(TakeOutType.SHORT).Build());
                        continue;
                    }
                    if (thing.matches("[^ a-zA-Z_-]+") || thing.contains("*")) {
                        ctx.receive(takeOutMessage.setSubType(TakeOutType.INVALID).Build());
                        continue;
                    }
                    try {
                        Optional<Item> maybeItem = container.getItems().stream()
                                .filter(item -> item.CheckNameRegex(thing, 3))
                                .findAny();
                        if (maybeItem.isEmpty()) {
                            if (thing.equalsIgnoreCase("all") || thing.equalsIgnoreCase("everything")) {
                                ctx.receive(takeOutMessage.setSubType(TakeOutType.GREEDY).Build());
                            } else {
                                ctx.receive(takeOutMessage.setSubType(TakeOutType.NOT_FOUND).Build());
                            }
                            continue;
                        }
                        Item item = maybeItem.get();
                        takeOutMessage.setItem(item);
                        if (item instanceof Takeable takeableItem) {
                            ctx.getCreature().addItem(takeableItem);
                            container.removeItem(takeableItem);
                            ctx.receive(takeOutMessage.setSubType(TakeOutType.FOUND_TAKEN).Build());
                            continue;
                        }
                        ctx.receive(takeOutMessage.setSubType(TakeOutType.NOT_TAKEABLE).Build());
                    } catch (PatternSyntaxException pse) {
                        pse.printStackTrace();
                        ctx.receive(takeOutMessage.setSubType(TakeOutType.UNCLEVER).Build());
                    }
                }
                while (container instanceof LockableItemContainer.Bypass bypass) {
                    container = bypass.getOrigin();
                }
                if (container instanceof LockableItemContainer liCon && liCon instanceof Item liConItem) {
                    if (liCon.isRemoveOnEmpty() && liCon.isEmpty()) {
                        Room.this.removeItem(liConItem);
                    }
                }
                return ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return Room.this;
        }
    }

    protected class InteractHandler implements RoomCommandHandler {
        private final static Predicate<CommandContext> enabledPredicate = InteractHandler.defaultNoBattlePredicate
                .and(ctx -> ctx.getRoom().getInteractableCount() > 0);
        private final static String helpString = "\"interact [item]\" Certain items in the room may be interactable. Like \"interact lever\"";

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.INTERACT;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            if (ctx == null || ctx.getCreature() == null) {
                return Optional.empty();
            }
            return Optional
                    .of(ctx.getCreature().isInBattle() ? InteractHandler.inBattleString : InteractHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return InteractHandler.enabledPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.INTERACT && cmd instanceof InteractMessage intMessage) {
                if (ctx.getCreature() == null) {
                    ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                            .setHelps(ctx.getHelps()).setCommand(cmd).Build());
                    return ctx.handled();
                }
                String name = intMessage.getObject();
                List<Item> matches = Room.this.getItems().stream()
                        .filter(ro -> ro != null && ro.CheckNameRegex(name, 3)).toList();

                if (matches.size() == 1) {
                    Item ro = matches.get(0);
                    if (ro instanceof InteractObject) {
                        InteractObject ex = (InteractObject) ro;
                        ctx.receive(ex.doUseAction(ctx.getCreature()));
                    } else {
                        ctx.receive(ItemInteractionEvent.getBuilder().setTaggable(ro)
                                .setSubType(InteractOutMessageType.CANNOT).Build());
                    }
                    return ctx.handled();
                }
                List<InteractObject> interactables = Room.this.getItems().stream()
                        .filter(ro -> ro != null && ro.checkVisibility() && ro instanceof InteractObject)
                        .map(ro -> (InteractObject) ro).toList();
                ctx.receive(BadTargetSelectedEvent.getBuilder().setBde(BadTargetOption.UNCLEAR).setBadTarget(name)
                        .setPossibleTargets(interactables).Build());
                return ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return Room.this;
        }
    }

    protected class DropHandler implements RoomCommandHandler {
        private static final String helpString = "\"drop [itemname]\" Drop an item that you have. Like \"drop longsword\"";
        private static final Predicate<CommandContext> enabledPredicate = DropHandler.defaultRoomPredicate
                .and(ctx -> ctx.getCreature().getItems().size() > 1);

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.DROP;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(DropHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return DropHandler.enabledPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.DROP && cmd instanceof DropMessage dMessage) {
                if (ctx.getCreature() == null) {
                    ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                            .setHelps(ctx.getHelps()).setCommand(dMessage).Build());
                    return ctx.handled();
                }
                ItemDroppedEvent.Builder dOutMessage = ItemDroppedEvent.getBuilder();
                if (dMessage.getDirects().size() == 0) {
                    ctx.receive(dOutMessage.setDropType(DropType.NO_ITEM));
                    return ctx.handled();
                }

                ItemContainer container = Room.this;
                dOutMessage.setDestination(Room.this.getName());
                Optional<String> containerName = dMessage.inContainer();
                if (containerName.isPresent()) {
                    // takeOutMessage.setSource(containerName.orElse(null));
                    dOutMessage.setDestination(containerName.get());
                    Optional<ItemContainer> foundContainer = Room.this.items.stream()
                            .filter(item -> item != null && item instanceof ItemContainer
                                    && item.checkName(containerName.get().replaceAll("^\"|\"$", "")))
                            .map(item -> (ItemContainer) item).findAny();
                    if (foundContainer.isEmpty()) {
                        ctx.receive(dOutMessage.setDropType(DropType.BAD_CONTAINER));
                        return ctx.handled();
                    } else if (foundContainer.get() instanceof LockableItemContainer liCon) {
                        if (!liCon.canAccess(ctx.getCreature())) {
                            ctx.receive(dOutMessage.setDropType(DropType.LOCKED_CONTAINER));
                            return ctx.handled();
                        }
                        container = liCon.getBypass();
                    } else {
                        container = foundContainer.get();
                    }
                }
                dOutMessage.setDestination(container.getName());

                for (String itemName : dMessage.getDirects()) {
                    Optional<Item> maybeTakeable = ctx.getCreature().removeItem(itemName);
                    if (maybeTakeable.isEmpty()) {
                        ctx.receive(ItemNotPossessedEvent.getBuilder().setItemType(Item.class.getSimpleName())
                                .setItemName(itemName).Build());
                        continue;
                    }
                    Item takeable = maybeTakeable.get();
                    container.addItem(takeable);
                    ctx.receive(dOutMessage.setDropType(DropType.SUCCESS).setItem(takeable)
                            .setDestination(container.getName()).Build());
                }
                return ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return Room.this;
        }
    }

    // only used to examine items and creatures in this room
    protected class SeeHandler implements RoomCommandHandler {
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
            return SeeHandler.defaultRoomPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.SEE && cmd instanceof SeeMessage sMessage) {
                if (sMessage.getThing() != null && !sMessage.getThing().isBlank()) {
                    String name = sMessage.getThing();
                    Collection<ICreature> found = Room.this.getCreaturesLike(name);
                    // we should be able to see people in a fight
                    if (found.size() == 1) {
                        ArrayList<ICreature> foundList = new ArrayList<ICreature>(found);
                        ctx.receive(((SeeEvent.Builder) foundList.get(0).produceMessage().copyBuilder())
                                .addExtraInfo("They are in the room with you. ").Build());
                        return ctx.handled();
                    }

                    if (ctx.getCreature() != null && ctx.getCreature().isInBattle()) {
                        ctx.receive(SeeEvent.getBuilder()
                                .setDeniedReason("You are in a fight right now, you are too busy to examine that!")
                                .Build());
                        return ctx.handled();
                    }

                    for (Item ro : items) {
                        if (ro.CheckNameRegex(name, 3)) {
                            ctx.receive(ro.produceMessage(SeeEvent.getBuilder().setExaminable(ro)
                                    .addExtraInfo("You see it in the room with you. ")));
                            return ctx.handled();
                        }
                    }

                    if (ctx.getCreature() != null) {
                        ICreature creature = ctx.getCreature();
                        for (Item thing : creature.getEquipmentSlots().values()) {
                            if (thing.CheckNameRegex(name, 3)) {
                                if (thing instanceof Examinable) {
                                    ctx.receive(((SeeEvent.Builder) thing.produceMessage().copyBuilder())
                                            .addExtraInfo("You have it equipped. ").Build());
                                    return ctx.handled();
                                }
                                ctx.receive(SeeEvent.getBuilder().setExaminable(thing)
                                        .addExtraInfo("You have it equipped. ").Build());
                                return ctx.handled();
                            }
                        }

                        Optional<Item> maybeThing = creature.getInventory().getItem(name);
                        if (maybeThing.isPresent()) {
                            Item thing = maybeThing.get();
                            if (thing instanceof Examinable) {
                                ctx.receive(((SeeEvent.Builder) thing.produceMessage().copyBuilder())
                                        .addExtraInfo("You see it in your inventory. ").Build());
                                return ctx.handled();
                            }
                            ctx.receive(SeeEvent.getBuilder().setExaminable(thing)
                                    .addExtraInfo("You see it in your inventory. ").Build());
                            return ctx.handled();
                        }
                    }

                    ctx.receive(
                            SeeEvent.getBuilder().setDeniedReason("You couldn't find " + name + " to examine. ")
                                    .Build());
                    return ctx.handled();
                } else {
                    ctx.receive(Room.this.produceMessage());
                    return ctx.handled();
                }
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return Room.this;
        }
    }

    protected class SayHandler implements RoomCommandHandler {
        private static final String helpString = new StringJoiner(" ")
                .add("\"say [message]\"").add("Tells everyone in your current room your message").add("\r\n")
                .add("\"say [message] to [name]\"")
                .add("Will tell a specific person somewhere in your current room your message.")
                .add("If your message contains the word 'to', put your message in quotes like")
                .add("\"say 'They are taking the hobbits to Isengard' to Aragorn\"")
                .add("\r\n")
                .toString();

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.SAY;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(SayHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return SayHandler.defaultRoomPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.SAY && cmd instanceof SayMessage sMessage) {
                SpeakingEvent.Builder speakMessage = SpeakingEvent.getBuilder().setSayer(ctx.getCreature())
                        .setMessage(sMessage.getMessage());
                if (sMessage.getTarget() != null) {
                    boolean sent = false;
                    Optional<ICreature> optTarget = Room.this.getCreature(sMessage.getTarget());
                    if (optTarget.isPresent()) {
                        CommandInvoker sayer = ctx.getClient();
                        if (ctx.getCreature() != null) {
                            sayer = ctx.getCreature();
                        } else if (ctx.getUser() != null) {
                            sayer = ctx.getUser();
                        }
                        ICreature target = optTarget.get();
                        speakMessage.setSayer(sayer).setHearer(target);
                        ICreature.eventAccepter.accept(target, speakMessage.Build());
                        sent = true;
                    }
                    if (!sent) {
                        ctx.receive(BadSpeakingTargetEvent.getBuilder().setCreatureName(sMessage.getTarget()));
                    }
                } else {
                    Room.this.announce(speakMessage.Build());
                }
                return ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return Room.this;
        }
    }

    protected class UseHandler implements RoomCommandHandler {
        private final static String helpString = new StringJoiner(" ")
                .add("\"use [itemname]\"").add("Uses an item that you have on yourself, if applicable.")
                .add("Like \"use potion\"").add("\r\n")
                .add("\"use [itemname] on [otherthing]\"")
                .add("Uses an item that you have on something or someone else, if applicable.")
                .add("Like \"use potion on Bob\"")
                .toString();
        private final static Predicate<CommandContext> enabledPredicate = UseHandler.defaultRoomPredicate.and(
                ctx -> ctx.getCreature().getItems().stream().anyMatch(item -> item != null && item instanceof Usable));

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.USE;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(UseHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return UseHandler.enabledPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.USE && cmd instanceof UseMessage useMessage) {
                if (ctx.getCreature() == null) {
                    ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                            .setHelps(ctx.getHelps()).setCommand(useMessage).Build());
                    return ctx.handled();
                }
                Optional<Item> maybeItem = ctx.getCreature().getItem(useMessage.getUsefulItem());
                if (maybeItem.isEmpty() || !(maybeItem.get() instanceof Usable)) {
                    ctx.receive(ItemUsedEvent.getBuilder().setSubType(UseOutMessageOption.NO_USES)
                            .setItemUser(ctx.getCreature()).Build());
                    return ctx.handled();
                }
                Usable usable = (Usable) maybeItem.get();
                if (useMessage.getTarget() == null || useMessage.getTarget().isBlank()) {
                    usable.doUseAction(ctx, ctx.getCreature());
                    return ctx.handled();
                }
                Collection<ICreature> maybeCreature = Room.this.getCreaturesLike(useMessage.getTarget());
                if (maybeCreature.size() == 1) {
                    List<ICreature> creatureList = new ArrayList<>(maybeCreature);
                    ICreature targetCreature = creatureList.get(0);
                    // if we aren't in battle, but our target is in battle, join the battle
                    if (!ctx.getCreature().isInBattle() && targetCreature.isInBattle()) {
                        Room.this.battleManager.addCreature(ctx.getCreature());
                        return Room.this.battleManager.handleChain(ctx, cmd);
                    }
                    usable.doUseAction(ctx, creatureList.get(0));
                    return ctx.handled();
                } else if (maybeCreature.size() > 1) {
                    ctx.receive(BadTargetSelectedEvent.getBuilder().setBde(BadTargetOption.UNCLEAR)
                            .setBadTarget(useMessage.getTarget()).setPossibleTargets(maybeCreature).Build());
                    return ctx.handled();
                }
                Optional<Item> maybeRoomItem = Room.this.getItem(useMessage.getTarget());
                if (maybeRoomItem.isPresent()) {
                    usable.doUseAction(ctx, maybeRoomItem.get());
                    return ctx.handled();
                }
                Optional<Item> maybeInventory = ctx.getCreature().getItem(useMessage.getTarget());
                if (maybeInventory.isPresent()) {
                    usable.doUseAction(ctx, maybeInventory.get());
                    return ctx.handled();
                }
                ctx.receive(BadTargetSelectedEvent.getBuilder().setBde(BadTargetOption.UNCLEAR)
                        .setBadTarget(useMessage.getTarget()).Build());
                return ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return Room.this;
        }
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
