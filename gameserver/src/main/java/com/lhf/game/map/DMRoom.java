package com.lhf.game.map;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.CreatureContainer;
import com.lhf.game.EntityEffect;
import com.lhf.game.Game;
import com.lhf.game.creature.DungeonMaster;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.INonPlayerCharacter.AbstractNPCBuilder;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.intelligence.handlers.LewdAIHandler;
import com.lhf.game.creature.intelligence.handlers.SilencedHandler;
import com.lhf.game.creature.intelligence.handlers.SpeakOnOtherEntry;
import com.lhf.game.creature.intelligence.handlers.SpokenPromptChunk;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.item.IItem;
import com.lhf.game.item.AItem;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.lewd.LewdBabyMaker;
import com.lhf.game.map.Area.AreaBuilder.PostBuildRoomOperations;
import com.lhf.game.map.RestArea.LewdStyle;
import com.lhf.game.map.SubArea.SubAreaBuilder;
import com.lhf.game.map.SubArea.SubAreaCasting;
import com.lhf.game.map.commandHandlers.AreaCastHandler;
import com.lhf.game.map.commandHandlers.AreaSayHandler;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.BadTargetSelectedEvent;
import com.lhf.messages.events.BadTargetSelectedEvent.BadTargetOption;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.RoomAffectedEvent;
import com.lhf.messages.events.RoomEnteredEvent;
import com.lhf.messages.events.RoomExitedEvent;
import com.lhf.messages.events.SpeakingEvent;
import com.lhf.messages.events.UserLeftEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.SayMessage;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.client.user.User;
import com.lhf.server.interfaces.NotNull;

public class DMRoom extends Room {
    private Set<User> users;
    private List<Land> lands;
    private transient Map<AMessageType, CommandHandler> commands;

    public static class DMRoomBuilder implements Area.AreaBuilder {
        private final transient Logger logger;
        private Room.RoomBuilder delegate;
        private List<Land.LandBuilder> landBuilders;

        private DMRoomBuilder() {
            this.logger = Logger.getLogger(this.getClass().getName());
            this.delegate = Room.RoomBuilder.getInstance();
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

        public DMRoomBuilder addItem(AItem item) {
            this.delegate = delegate.addItem(item);
            return this;
        }

        public DMRoomBuilder addSubAreaBuilder(SubAreaBuilder<?, ?> builder) {
            this.delegate.addSubAreaBuilder(builder);
            return this;
        }

        public DMRoomBuilder addNPCBuilder(INonPlayerCharacter.AbstractNPCBuilder<?, ?> builder) {
            this.delegate = delegate.addNPCBuilder(builder);
            return this;
        }

        public DMRoomBuilder addDungeonMasterBuilder(DungeonMaster.DungeonMasterBuilder builder) {
            this.delegate = delegate.addNPCBuilder(builder);
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

        @Override
        public String getDescription() {
            return this.delegate.getDescription();
        }

        @Override
        public Collection<IItem> getItems() {
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

        @Override
        public Collection<SubAreaBuilder<?, ?>> getSubAreasToBuild() {
            return delegate.getSubAreasToBuild();
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
                room.addCreatures(creaturesBuilt, true);
                for (final SubAreaBuilder<?, ?> subAreaBuilder : this.getSubAreasToBuild()) {
                    room.addSubArea(subAreaBuilder);
                }
            }, () -> (dmRoom) -> {
                final List<Land> landsBuilt = this.quickBuildLands(aiRunner, dmRoom);
                for (Land toAdd : landsBuilt) {
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
                room.addCreatures(creaturesBuilt, true);
                for (final SubAreaBuilder<?, ?> subAreaBuilder : this.getSubAreasToBuild()) {
                    room.addSubArea(subAreaBuilder);
                }
            }, () -> (dmRoom) -> {
                final List<Land> landsBuilt = this.buildLands(aiRunner, dmRoom, null, statblockManager,
                        conversationManager);
                for (Land toAdd : landsBuilt) {
                    dmRoom.addLand(toAdd);
                }
            });
        }

        @Override
        public DMRoom build(Land land, AIRunner aiRunner, StatblockManager statblockManager,
                ConversationManager conversationManager) throws FileNotFoundException {
            return this.build(land, land, aiRunner, statblockManager, conversationManager);
        }

        public static DMRoomBuilder buildDefault(AIRunner aiRunner, StatblockManager statblockManager,
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
            RestArea.Builder restBuilder = RestArea.getBuilder().setLewd(LewdStyle.QUICKIE)
                    .setLewdProduct(new LewdBabyMaker());
            CreatureFilterQuery query = new CreatureFilterQuery();
            query.filters.add(CreatureFilters.NAME);
            query.name = "Lovejax";
            query.nameRegexLen = 7;
            restBuilder.addCreatureQuery(query).setAllowCasting(SubAreaCasting.FLUSH_CASTING).setQueryOnBuild(false);
            builder.addSubAreaBuilder(restBuilder);

            builder.addDungeonMasterBuilder(dmAda).addDungeonMasterBuilder(dmGary);

            return builder;
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
        if (this.filterCreatures(EnumSet.of(CreatureContainer.CreatureFilters.TYPE), null, null, null, null,
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
                Optional<IItem> maybeCorpse = this.getItem(name);
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

    protected class SayHandler extends AreaSayHandler {

        @Override
        public boolean isEnabled(CommandContext ctx) {
            if (ctx == null) {
                return false;
            }
            final User user = ctx.getUser();
            final ICreature creature = ctx.getCreature();
            final Area area = ctx.getArea();
            if (area == null) {
                return false;
            }
            return creature != null || user != null;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == this.getHandleType()) {
                final SayMessage sayMessage = new SayMessage(cmd);
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
        public CommandChainHandler getChainHandler(CommandContext ctx) {
            return DMRoom.this;
        }
    }

    protected class CastHandler extends AreaCastHandler {

        @Override
        public boolean isEnabled(CommandContext ctx) {
            return super.isEnabled(ctx) && ctx.getCreature().getVocation() != null
                    && VocationName.DUNGEON_MASTER.equals(ctx.getCreature().getVocation().getVocationName());
        }

        @Override
        public CommandChainHandler getChainHandler(CommandContext ctx) {
            return DMRoom.this;
        }
    }

    @Override
    protected Map<AMessageType, CommandHandler> buildCommands() {
        Map<AMessageType, CommandHandler> gathered = super.buildCommands();
        gathered.put(AMessageType.SAY, new DMRoom.SayHandler());
        gathered.put(AMessageType.CAST, new DMRoom.CastHandler());
        return gathered;
    }

    @Override
    public Map<AMessageType, CommandHandler> getCommands(CommandContext ctx) {
        return Collections.unmodifiableMap(this.commands);
    }

}
