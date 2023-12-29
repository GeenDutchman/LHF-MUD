package com.lhf.game.map;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.CreatureContainer;
import com.lhf.game.EntityEffect;
import com.lhf.game.Game;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.INonPlayerCharacter.AbstractNPCBuilder;
import com.lhf.game.creature.DungeonMaster;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.intelligence.handlers.LewdAIHandler;
import com.lhf.game.creature.intelligence.handlers.SilencedHandler;
import com.lhf.game.creature.intelligence.handlers.SpeakOnOtherEntry;
import com.lhf.game.creature.intelligence.handlers.SpokenPromptChunk;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.item.Item;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.item.concrete.LewdBed;
import com.lhf.game.lewd.LewdBabyMaker;
import com.lhf.game.magic.ThirdPower;
import com.lhf.game.map.Area.AreaBuilder.PostBuildRoomOperations;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.BadTargetSelectedEvent;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.RoomAffectedEvent;
import com.lhf.messages.events.RoomEnteredEvent;
import com.lhf.messages.events.RoomExitedEvent;
import com.lhf.messages.events.SpeakingEvent;
import com.lhf.messages.events.UserLeftEvent;
import com.lhf.messages.events.BadTargetSelectedEvent.BadTargetOption;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.GameEventType;
import com.lhf.messages.in.SayMessage;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.client.user.User;
import com.lhf.server.interfaces.NotNull;

public class DMRoom extends Room {
    private Set<User> users;
    private List<Land> lands;
    private transient Map<CommandMessage, CommandHandler> commands;

    public static class DMRoomBuilder implements Area.AreaBuilder {
        private final transient Logger logger;
        private Room.RoomBuilder delegate;
        private List<Land> prebuiltLands;
        private List<Land.LandBuilder> landBuilders;

        private DMRoomBuilder() {
            this.logger = Logger.getLogger(this.getClass().getName());
            this.delegate = Room.RoomBuilder.getInstance();
            this.prebuiltLands = new ArrayList<>();
            this.landBuilders = new ArrayList<>();
        }

        public static DMRoomBuilder getInstance() {
            return new DMRoomBuilder();
        }

        public DMRoomBuilder setName(String name) {
            this.delegate = delegate.setName(name);
            return this;
        }

        public DMRoomBuilder setDescription(String description) {
            this.delegate = delegate.setDescription(description);
            return this;
        }

        public DMRoomBuilder addItem(Item item) {
            this.delegate = delegate.addItem(item);
            return this;
        }

        /**
         * Adds a prebuilt NPC to the Builder
         * 
         * @deprecated We want to keep the Builders Serializable, and a full Creature
         *             has several non-Serializable components that are neccesary for
         *             functionality. Prefer using
         *             {@link #addNPCBuilder(AbstractNPCBuilder)}.
         */
        @Deprecated(forRemoval = true)
        public DMRoomBuilder addPrebuiltNPC(INonPlayerCharacter creature) {
            this.delegate = delegate.addPrebuiltNPC(creature);
            return this;
        }

        public DMRoomBuilder addNPCBuilder(INonPlayerCharacter.AbstractNPCBuilder<?, ?> builder) {
            this.delegate = delegate.addNPCBuilder(builder);
            return this;
        }

        /**
         * Adds a prebuilt DM to the Builder
         * 
         * @deprecated We want to keep the Builders Serializable, and a full
         *             DungeonMaster
         *             has several non-Serializable components that are neccesary for
         *             functionality. Prefer using
         *             {@link #addDungeonMasterBuilder(DungeonMaster.DungeonMasterBuilder)}.
         */
        @Deprecated(forRemoval = true)
        public DMRoomBuilder addDungeonMaster(DungeonMaster dm) {
            this.delegate = delegate.addPrebuiltNPC(dm);
            return this;
        }

        public DMRoomBuilder addDungeonMasterBuilder(DungeonMaster.DungeonMasterBuilder builder) {
            this.delegate = delegate.addNPCBuilder(builder);
            return this;
        }

        @Deprecated(forRemoval = true)
        public DMRoomBuilder addDungeon(Dungeon dungeon) {
            if (this.prebuiltLands == null) {
                this.prebuiltLands = new ArrayList<>();
            }
            if (dungeon != null) {
                this.prebuiltLands.add(dungeon);
            }
            return this;
        }

        public DMRoomBuilder addLandBuilder(Land.LandBuilder builder) {
            if (builder != null) {
                this.landBuilders.add(builder);
            }
            return this;
        }

        public List<Land.LandBuilder> getLandBuilders() {
            return Collections.unmodifiableList(this.landBuilders);
        }

        @Deprecated(forRemoval = true)
        @Override
        public Collection<INonPlayerCharacter> getPrebuiltNPCs() {
            return this.delegate.getPrebuiltNPCs();
        }

        @Override
        public String getDescription() {
            return this.delegate.getDescription();
        }

        @Override
        public Collection<Item> getItems() {
            return this.delegate.getItems();
        }

        @Override
        public String getName() {
            return this.delegate.getName();
        }

        @Override
        public AreaBuilderID getAreaBuilderID() {
            return delegate.getAreaBuilderID();
        }

        @Override
        public Collection<AbstractNPCBuilder<?, ?>> getNPCsToBuild() {
            return delegate.getNPCsToBuild();
        }

        private List<Land> quickBuildLands(AIRunner aiRunner, DMRoom dmRoom) {
            List<Land.LandBuilder> toBuild = this.getLandBuilders();
            if (toBuild == null) {
                return List.of();
            }
            List<Land> built = new ArrayList<>();
            for (final Land.LandBuilder builder : toBuild) {
                if (builder == null) {
                    continue;
                }
                built.add(builder.quickBuild(dmRoom, aiRunner));
            }
            return Collections.unmodifiableList(built);
        }

        @Override
        public Area quickBuild(CommandChainHandler successor, Land land, AIRunner aiRunner) {
            this.logger.log(Level.INFO, () -> String.format("QUICK Building DM room '%s'", this.getName()));
            return DMRoom.quickBuilder(this, () -> land, () -> successor, () -> (room) -> {
                final Set<INonPlayerCharacter> creaturesBuilt = this.delegate.quickBuildCreatures(aiRunner, room);
                final Set<ICreature> creaturesToAdd = new TreeSet<>(this.getPrebuiltNPCs());
                creaturesToAdd.addAll(creaturesBuilt);
                room.addCreatures(creaturesToAdd, true);
            }, () -> (dmRoom) -> {
                final List<Land> landsBuilt = this.quickBuildLands(aiRunner, dmRoom);
                final List<Land> landsToAdd = new ArrayList<>(this.prebuiltLands);
                landsToAdd.addAll(landsBuilt);
                for (Land toAdd : landsToAdd) {
                    dmRoom.addLand(toAdd);
                }
            });
        }

        private List<Land> buildLands(AIRunner aiRunner, DMRoom dmRoom, Game game,
                StatblockManager statblockManager, ConversationManager conversationManager)
                throws FileNotFoundException {
            List<Land.LandBuilder> toBuild = this.getLandBuilders();
            if (toBuild == null) {
                return List.of();
            }
            List<Land> built = new ArrayList<>();
            for (final Land.LandBuilder builder : toBuild) {
                if (builder == null) {
                    continue;
                }
                built.add(builder.build(game, aiRunner, statblockManager, conversationManager));
            }
            return Collections.unmodifiableList(built);
        }

        @Override
        public DMRoom build(CommandChainHandler successor, Land land, AIRunner aiRunner,
                StatblockManager statblockManager, ConversationManager conversationManager)
                throws FileNotFoundException {
            this.logger.log(Level.INFO, () -> String.format("Building DM room '%s'", this.getName()));
            return DMRoom.fromBuilder(this, () -> land, () -> successor, () -> (room) -> {
                final Set<INonPlayerCharacter> creaturesBuilt = this.delegate.buildCreatures(aiRunner, room,
                        statblockManager,
                        conversationManager);
                final Set<ICreature> creaturesToAdd = new TreeSet<>(this.getPrebuiltNPCs());
                creaturesToAdd.addAll(creaturesBuilt);
                room.addCreatures(creaturesToAdd, true);
            }, () -> (dmRoom) -> {
                final List<Land> landsBuilt = this.buildLands(aiRunner, dmRoom, null, statblockManager,
                        conversationManager);
                final List<Land> landsToAdd = new ArrayList<>(this.prebuiltLands);
                landsToAdd.addAll(landsBuilt);
                for (Land toAdd : landsToAdd) {
                    dmRoom.addLand(toAdd);
                }
            });
        }

        @Override
        public DMRoom build(Land land, AIRunner aiRunner, StatblockManager statblockManager,
                ConversationManager conversationManager) throws FileNotFoundException {
            return this.build(land, land, aiRunner, statblockManager, conversationManager);
        }

        public static DMRoom buildDefault(AIRunner aiRunner, StatblockManager statblockManager,
                ConversationManager conversationManager)
                throws FileNotFoundException {
            DMRoomBuilder builder = DMRoomBuilder.getInstance();
            builder.setName("Control Room")
                    .setDescription("There are a lot of buttons and screens in here.  It looks like a home office.");

            DungeonMaster.DungeonMasterBuilder dmAda = DungeonMaster.DungeonMasterBuilder.getInstance();
            if (conversationManager != null) {
                dmAda.setConversationTree(conversationManager.convoTreeFromFile("verbal_default"));
            }
            SilencedHandler noSleepNoise = new SilencedHandler(GameEventType.INTERACT);
            dmAda.addAIHandler(noSleepNoise);
            LewdAIHandler lewdAIHandler = new LewdAIHandler().setPartnersOnly().setStayInAfter();
            dmAda.addAIHandler(lewdAIHandler);
            dmAda.addAIHandler(new SpokenPromptChunk().setAllowUsers());
            dmAda.addAIHandler(new SpeakOnOtherEntry());
            dmAda.setName("Ada Lovejax");

            DungeonMaster.DungeonMasterBuilder dmGary = DungeonMaster.DungeonMasterBuilder.getInstance();
            if (conversationManager != null) {
                dmGary.setConversationTree(conversationManager.convoTreeFromFile("gary"));
            }
            dmGary.addAIHandler(noSleepNoise);
            dmGary.addAIHandler(lewdAIHandler);
            dmGary.addAIHandler(new SpokenPromptChunk().setAllowUsers());
            dmGary.addAIHandler(new SpeakOnOtherEntry());
            dmGary.setName("Gary Lovejax");

            lewdAIHandler.addPartner(dmGary.getName()).addPartner(dmAda.getName());

            builder.addDungeonMasterBuilder(dmAda).addDungeonMasterBuilder(dmGary);

            DMRoom built = builder.build(null, null, aiRunner, statblockManager, conversationManager);

            LewdBed.Builder bedBuilder = LewdBed.Builder.getInstance().setCapacity(2)
                    .setLewdProduct(new LewdBabyMaker());
            for (ICreature dm : built.getCreaturesLike("Lovejax")) {
                if (dm != null) {
                    bedBuilder.addOccupant(dm);
                }
            }
            LewdBed bed = bedBuilder.build(built);

            built.addItem(bed);

            return built;
        }
    }

    static DMRoom fromBuilder(DMRoomBuilder builder, Supplier<Land> landSupplier,
            Supplier<CommandChainHandler> successorSupplier,
            Supplier<PostBuildRoomOperations<? super Room>> postRoomOperations,
            Supplier<PostBuildRoomOperations<? super DMRoom>> postDMRoomOperations) throws FileNotFoundException {
        DMRoom dmRoom = new DMRoom(builder, landSupplier, successorSupplier);
        if (postRoomOperations != null) {
            PostBuildRoomOperations<? super Room> postRoomOp = postRoomOperations.get();
            if (postRoomOp != null) {
                postRoomOp.accept(dmRoom);
            }
        }
        if (postDMRoomOperations != null) {
            PostBuildRoomOperations<? super DMRoom> postDMRoomOp = postDMRoomOperations.get();
            if (postDMRoomOp != null) {
                postDMRoomOp.accept(dmRoom);
            }
        }
        return dmRoom;
    }

    static DMRoom quickBuilder(DMRoomBuilder builder, Supplier<Land> landSupplier,
            Supplier<CommandChainHandler> successorSupplier,
            Supplier<Consumer<? super Room>> postRoomOperations,
            Supplier<Consumer<? super DMRoom>> postDMRoomOperations) {
        DMRoom dmRoom = new DMRoom(builder, landSupplier, successorSupplier);
        if (postRoomOperations != null) {
            Consumer<? super Room> postRoomOp = postRoomOperations.get();
            if (postRoomOp != null) {
                postRoomOp.accept(dmRoom);
            }
        }
        if (postDMRoomOperations != null) {
            Consumer<? super DMRoom> postDMRoomOp = postDMRoomOperations.get();
            if (postDMRoomOp != null) {
                postDMRoomOp.accept(dmRoom);
            }
        }
        return dmRoom;
    }

    DMRoom(DMRoomBuilder builder, Supplier<Land> landSupplier,
            Supplier<CommandChainHandler> successorSupplier) {
        super(builder.delegate, landSupplier, successorSupplier);
        this.lands = new ArrayList<>();
        this.users = new HashSet<>();
        this.commands = this.buildCommands();
    }

    public boolean addLand(@NotNull Land land) {
        land.setSuccessor(this);
        return this.lands.add(land);
    }

    public boolean addUser(User user) {
        if (this.filterCreatures(EnumSet.of(CreatureContainer.Filters.TYPE), null, null, null, null,
                DungeonMaster.class, null).size() < 2) {
            this.log(Level.INFO, () -> "Conditions met to create and add Player automatically");
            return this.addNewPlayer(Player.PlayerBuilder.getInstance(user).build(null));
        }
        boolean added = this.users.add(user);
        if (added) {
            user.setSuccessor(this);
            this.log(Level.FINE, () -> String.format("User %s entered DMRoom", user));
            this.announce(RoomEnteredEvent.getBuilder().setNewbie(user).setBroacast().Build());
        }
        return added;
    }

    public User getUser(String username) {
        for (User user : this.users) {
            if (username.equals(user.getUsername())) {
                return user;
            }
        }
        return null;
    }

    public User removeUser(String username) {
        for (User user : this.users) {
            if (username.equals(user.getUsername())) {
                this.users.remove(user);
                this.announce(RoomExitedEvent.getBuilder().setLeaveTaker(user).setBroacast().Build());
                return user;
            }
        }
        return null;
    }

    public boolean addNewPlayer(Player player) {
        return this.lands.get(0).addPlayer(player);
    }

    public void userExitSystem(User user) {
        for (Land land : this.lands) {
            if (land.removePlayer(user.getUserID()).isPresent()) {
                land.announce(UserLeftEvent.getBuilder().setUser(user).setBroacast().Build());
            }
        }
    }

    @Override
    public Collection<GameEventProcessor> getGameEventProcessors() {
        Collection<GameEventProcessor> messengers = new TreeSet<>(GameEventProcessor.getComparator());
        messengers.addAll(super.getGameEventProcessors());
        this.users.stream().filter(userThing -> userThing != null)
                .forEach(userThing -> messengers.add(userThing));
        return messengers;
    }

    @Override
    public boolean isCorrectEffectType(EntityEffect effect) {
        return effect != null && effect instanceof DMRoomEffect;
    }

    @Override
    public RoomAffectedEvent processEffect(EntityEffect effect, boolean reverse) {
        if (this.isCorrectEffectType(effect)) {
            DMRoomEffect dmRoomEffect = (DMRoomEffect) effect;
            this.logger.log(Level.FINER, () -> String.format("DMRoom processing effect '%s'", dmRoomEffect.getName()));
            if (dmRoomEffect.getEnsoulUsername() != null) {
                String name = dmRoomEffect.getEnsoulUsername();
                User user = this.getUser(name);
                if (user == null) {
                    this.logger.log(Level.FINEST,
                            () -> String.format("A user by the name of '%s' was not found", name));
                    if (dmRoomEffect.creatureResponsible() != null) {
                        GameEvent whoops = BadTargetSelectedEvent.getBuilder().setBde(BadTargetOption.DNE)
                                .setBadTarget(name).Build();
                        ICreature.eventAccepter.accept(dmRoomEffect.creatureResponsible(), whoops);
                        return null;
                    }
                }
                Optional<Item> maybeCorpse = this.getItem(name);
                if (maybeCorpse.isEmpty() || !(maybeCorpse.get() instanceof Corpse)) {
                    this.logger.log(Level.FINEST, () -> String.format("No corpse was found with the name '%s'", name));
                    if (effect.creatureResponsible() != null) {
                        ICreature.eventAccepter.accept(dmRoomEffect.creatureResponsible(),
                                BadTargetSelectedEvent.getBuilder()
                                        .setBde(BadTargetOption.DNE).setBadTarget(name).Build());
                        return null;
                    }
                }
                Corpse corpse = (Corpse) maybeCorpse.get();
                Player player = Player.PlayerBuilder.getInstance(user).setVocation(dmRoomEffect.getVocation())
                        .setCorpse(corpse).build(null);
                this.removeItem(corpse);
                this.addNewPlayer(player);
            }
        }
        return super.processEffect(effect, reverse);
    }

    protected class SayHandler extends Room.SayHandler {
        private static final Predicate<CommandContext> enabledPredicate = SayHandler.defaultPredicate
                .and(ctx -> ctx.getUser() != null).and(ctx -> ctx.getRoom() != null)
                .or(SayHandler.defaultRoomPredicate);

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.SAY && cmd instanceof SayMessage sayMessage) {
                if (sayMessage.getTarget() != null && !sayMessage.getTarget().isBlank()) {
                    boolean sent = false;
                    for (User u : DMRoom.this.users) {
                        if (u.getUsername().equals(sayMessage.getTarget())) {
                            CommandInvoker sayer = ctx.getClient();
                            if (ctx.getCreature() != null) {
                                sayer = ctx.getCreature();
                            } else if (ctx.getUser() != null) {
                                sayer = ctx.getUser();
                            }
                            User.eventAccepter.accept(u,
                                    SpeakingEvent.getBuilder().setSayer(sayer).setMessage(sayMessage.getMessage())
                                            .setHearer(u).Build());
                            sent = true;
                            break;
                        }
                    }
                    if (sent) {
                        return ctx.handled();
                    }
                }
            }
            return super.handleCommand(ctx, cmd);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return SayHandler.enabledPredicate;
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return DMRoom.this;
        }
    }

    protected class CastHandler extends Room.CastHandler {
        private final static Predicate<CommandContext> enabledPredicate = Room.CastHandler.defaultRoomPredicate
                .and(ctx -> ctx.getCreature() instanceof DungeonMaster);

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return CastHandler.enabledPredicate;
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return DMRoom.this;
        }
    }

    @Override
    protected Map<CommandMessage, CommandHandler> buildCommands() {
        Map<CommandMessage, CommandHandler> gathered = super.buildCommands();
        gathered.put(CommandMessage.SAY, new DMRoom.SayHandler());
        gathered.put(CommandMessage.CAST, new DMRoom.CastHandler());
        return gathered;
    }

    @Override
    public Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx) {
        return Collections.unmodifiableMap(this.commands);
    }

}
