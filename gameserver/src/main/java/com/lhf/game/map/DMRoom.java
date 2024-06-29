package com.lhf.game.map;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.Atlas;
import com.lhf.game.Atlas.AtlasException;
import com.lhf.game.AtlasTrawlerBuilder;
import com.lhf.game.CreatureContainer;
import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.creature.CreatureFactory;
import com.lhf.game.creature.DungeonMaster;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.INonPlayerCharacter.INonPlayerCharacterBuildInfo;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.Player.PlayerBuildInfo;
import com.lhf.game.creature.QuestEffect;
import com.lhf.game.creature.QuestSource;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.intelligence.handlers.LewdAIHandler;
import com.lhf.game.creature.intelligence.handlers.SilencedHandler;
import com.lhf.game.creature.intelligence.handlers.SpeakOnOtherEntry;
import com.lhf.game.creature.intelligence.handlers.SpokenPromptChunk;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.AItem;
import com.lhf.game.item.IItem;
import com.lhf.game.item.ItemPartitionListVisitor;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.lewd.LewdBabyMaker;
import com.lhf.game.map.Land.LandBuilder;
import com.lhf.game.map.Land.LandBuilder.LandBuilderID;
import com.lhf.game.map.RestArea.LewdStyle;
import com.lhf.game.map.SubArea.ISubAreaBuildInfo;
import com.lhf.game.map.SubArea.SubAreaCasting;
import com.lhf.game.map.commandHandlers.AreaCastHandler;
import com.lhf.game.map.commandHandlers.AreaSayHandler;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.BadTargetSelectedEvent;
import com.lhf.messages.events.BadTargetSelectedEvent.BadTargetOption;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.GameEventTester;
import com.lhf.messages.events.RoomAffectedEvent;
import com.lhf.messages.events.RoomEnteredEvent;
import com.lhf.messages.events.RoomExitedEvent;
import com.lhf.messages.events.SpeakingEvent;
import com.lhf.messages.events.UserLeftEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.SayMessage;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.client.user.User;

public class DMRoom extends Room {

    private final class LandAtlas extends Atlas<Land, String, Directions, Doorway> {

        @Override
        public String getIDForMemberType(Land member) {
            return member != null ? member.getName() : null;
        }

        @Override
        public String getNameForMemberType(Land member) {
            return member != null ? member.getName() : null;
        }

        @Override
        public Directions translateLinkToOpposite(Directions link) {
            if (link == null) {
                return null;
            }
            return link.opposite();
        }

        @Override
        public Atlas<Land, String, Directions, Doorway>.AtlasToMermaidWriter generateMermaidWriter(String indent) {
            return this.new AtlasToMermaidWriter(indent) {

                @Override
                protected String displayID(String id) {
                    return id;
                }

                @Override
                protected String displayLink(Directions link) {
                    return link != null ? link.toString() : "null";
                }

                @Override
                protected String displayTraversalTest(Doorway traversal) {
                    return "";
                }

                @Override
                protected String displayMemberNote(Land member) {
                    return "";
                }

            };
        }

    }

    private transient Set<User> users;
    private final LandAtlas lands;
    private transient Map<AMessageType, CommandHandler> commands;

    public static class DMRoomBuilder implements Area.AreaBuilder {
        public final static class LandBuilderAtlas extends Atlas<LandBuilder, LandBuilderID, Directions, Doorway>
                implements Serializable {

            @Override
            public LandBuilderID getIDForMemberType(LandBuilder member) {
                return member != null ? member.getLandBuilderID() : null;
            }

            @Override
            public String getNameForMemberType(LandBuilder member) {
                return member != null ? member.getName() : null;
            }

            @Override
            public Directions translateLinkToOpposite(Directions link) {
                return link != null ? link.opposite() : null;
            }

            @Override
            public Atlas<LandBuilder, LandBuilderID, Directions, Doorway>.AtlasToMermaidWriter generateMermaidWriter(
                    String indent) {
                return new AtlasToMermaidWriter(indent) {

                    @Override
                    protected String displayID(LandBuilderID id) {
                        return id != null ? id.toString() : "null";
                    }

                    @Override
                    protected String displayLink(Directions link) {
                        return link != null ? link.toString() : "null";
                    }

                    @Override
                    protected String displayTraversalTest(Doorway traversal) {
                        return "";
                    }

                    @Override
                    protected String displayMemberNote(LandBuilder member) {
                        return "";
                    }

                };
            }

        };

        private final String className;
        private final transient Logger logger;
        private Room.RoomBuilder delegate;
        private LandBuilderAtlas landBuilders;

        private DMRoomBuilder() {
            this.className = this.getClass().getName();
            this.logger = Logger.getLogger(this.className);
            this.delegate = Room.RoomBuilder.getInstance();
            this.landBuilders = new LandBuilderAtlas();
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

        public DMRoomBuilder addSubAreaBuilder(ISubAreaBuildInfo builder) {
            this.delegate.addSubAreaBuilder(builder);
            return this;
        }

        public DMRoomBuilder addNPCBuilder(INonPlayerCharacterBuildInfo builder) {
            this.delegate = delegate.addNPCBuilder(builder);
            return this;
        }

        public DMRoomBuilder addDungeonMasterBuilder(DungeonMaster.DungeonMasterBuildInfo builder) {
            this.delegate = delegate.addNPCBuilder(builder);
            return this;
        }

        public AtlasTrawlerBuilder<LandBuilder, LandBuilderID, Directions, Doorway> getTrawlerBuilder() {
            return this.landBuilders.getTrawlerBuilder();
        }

        public DMRoomBuilder arrangeLandsInline(
                Consumer<AtlasTrawlerBuilder<LandBuilder, LandBuilderID, Directions, Doorway>> arranger) {
            if (arranger != null) {
                arranger.accept(this.getTrawlerBuilder());
            }
            return this;
        }

        public DMRoomBuilder addForbiddenCommandType(AMessageType type) {
            this.delegate = delegate.addForbiddenCommandType(type);
            return this;
        }

        public DMRoomBuilder clearForbiddenCommandTypes() {
            this.delegate = delegate.clearForbiddenCommandTypes();
            return this;
        }

        public DMRoomBuilder doNotForbidCommandType(AMessageType type) {
            this.delegate = delegate.doNotForbidCommandType(type);
            return this;
        }

        @Override
        public Set<AMessageType> getForbiddenCommandTypes() {
            return this.delegate.getForbiddenCommandTypes();
        }

        public LandBuilderAtlas getLandBuilders() {
            return this.landBuilders;
        }

        @Override
        public String getDescription() {
            return this.delegate.getDescription();
        }

        @Override
        public ItemPartitionListVisitor getItems() {
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
        public Collection<INonPlayerCharacterBuildInfo> getNPCsToBuild() {
            return delegate.getNPCsToBuild();
        }

        @Override
        public Collection<ISubAreaBuildInfo> getSubAreasToBuild() {
            return delegate.getSubAreasToBuild();
        }

        private void buildLands(DMRoom dmRoom, CommandChainHandler successor, AIRunner aiRunner,
                ConversationManager conversationManager, boolean fallbackNoConversation) throws AtlasException {
            LandBuilderAtlas toBuild = this.getLandBuilders();
            if (toBuild.size() == 0) {
                toBuild.addMember(Dungeon.DungeonBuilder.newInstance().addStartingRoom(
                        Room.RoomBuilder.getInstance().setDescription("What a boring, default room!")));
            }
            if (dmRoom.lands == null) {
                throw new IllegalStateException("The DMRoom to be built cannot already have a null land atlas!?!");
            }

            toBuild.translate(dmRoom.lands,
                    landBuilder -> landBuilder != null ? landBuilder.build(successor != null ? successor : dmRoom,
                            aiRunner, conversationManager, fallbackNoConversation) : null,
                    dir -> dir, door -> door);

            final BiFunction<String, String, Area> lookupFunction = (landName, areaName) -> {
                if (landName == null || areaName == null) {
                    return null;
                }
                final Land land = dmRoom.lands.getAtlasMemberOrNull(landName);
                if (land != null) {
                    return land.getAreaByName(areaName).orElse(null);
                }
                return null;
            };
            for (final Land land : dmRoom.lands) {
                if (land == null) {
                    continue;
                }
                land.getAtlas().populateExternalReferences(lookupFunction);
            }
        }

        @Override
        public DMRoom build(CommandChainHandler successor, Land land, AIRunner aiRunner,
                ConversationManager conversationManager, boolean fallbackNoConversation) {
            this.logger.log(Level.INFO, () -> String.format("Building DM room '%s'", this.getName()));
            return DMRoom.fromBuilder(this, () -> land, () -> successor, () -> (room) -> {
                final Set<INonPlayerCharacter> creaturesBuilt = this.delegate.buildCreatures(aiRunner, room,
                        conversationManager, fallbackNoConversation);
                room.addCreatures(creaturesBuilt, false);
                for (final ISubAreaBuildInfo subAreaBuilder : this.getSubAreasToBuild()) {
                    room.addSubArea(subAreaBuilder);
                }
            }, () -> (dmRoom) -> {
                try {
                    this.buildLands(dmRoom, successor, aiRunner, conversationManager, fallbackNoConversation);
                } catch (AtlasException e) {
                    final String errDesc = String.format("Cannot build lands for DMRoom with builder '%s'", this);
                    this.logger.log(Level.SEVERE, errDesc, e);
                    throw new IllegalStateException(errDesc, e);
                }
            });
        }

        @Override
        public DMRoom quickBuild(CommandChainHandler successor, Land land, AIRunner aiRunner) {
            return this.build(successor, land, aiRunner, null, true);
        }

        @Override
        public DMRoom build(Land land, AIRunner aiRunner, ConversationManager conversationManager) {
            return this.build(land, land, aiRunner, conversationManager, true);
        }

        public static DMRoomBuilder buildDefault(AIRunner aiRunner, ConversationManager conversationManager)
                throws FileNotFoundException {
            DMRoomBuilder builder = DMRoomBuilder.getInstance();
            builder.setName("Control Room")
                    .setDescription("There are a lot of buttons and screens in here.  It looks like a home office.");

            DungeonMaster.DungeonMasterBuildInfo dmAda = DungeonMaster.DungeonMasterBuildInfo.getInstance();
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

            DungeonMaster.DungeonMasterBuildInfo dmGary = DungeonMaster.DungeonMasterBuildInfo.getInstance();
            if (conversationManager != null) {
                dmGary.setConversationTree(conversationManager.convoTreeFromFile("gary"));
            }
            dmGary.addAIHandler(noSleepNoise);
            dmGary.addAIHandler(lewdAIHandler);
            dmGary.addAIHandler(new SpokenPromptChunk().setAllowUsers());
            dmGary.addAIHandler(new SpeakOnOtherEntry());
            dmGary.setName("Gary Lovejax");

            lewdAIHandler.addPartner(dmGary.getName()).addPartner(dmAda.getName());
            final GameEventTester questTester = new GameEventTester(GameEventType.FIGHT_OVER,
                    Set.of("You have survived this battle"), null, TickType.BATTLE, false);
            QuestSource questSource = QuestSource.getQuestBuilder("Survive").setDescription("Survive two battles")
                    .setDeltaForTester(questTester,
                            new Deltas().setStatChange(Stats.MAXHP, 5).setStatChange(Stats.CURRENTHP, 5))
                    .setPersistence(new EffectPersistence(2, questTester)).build();
            RestArea.Builder restBuilder = RestArea.getBuilder().setLewd(LewdStyle.QUICKIE)
                    .setLewdProduct(new LewdBabyMaker(Player.getPlayerBuilder(null)
                            .applyEffect(new QuestEffect(questSource, null, questSource))));
            CreatureFilterQuery query = new CreatureFilterQuery();
            query.filters.add(CreatureFilters.NAME);
            query.name = "Lovejax";
            query.nameRegexLen = 7;
            restBuilder.addCreatureQuery(query).setAllowCasting(SubAreaCasting.FLUSH_CASTING).setQueryOnBuild(false)
                    .setLoggingLevel(Level.INFO);
            builder.addSubAreaBuilder(restBuilder);

            builder.addDungeonMasterBuilder(dmAda).addDungeonMasterBuilder(dmGary);

            return builder;
        }

        @Override
        public int hashCode() {
            return Objects.hash(className, delegate, landBuilders);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof DMRoomBuilder))
                return false;
            DMRoomBuilder other = (DMRoomBuilder) obj;
            return Objects.equals(className, other.className) && Objects.equals(delegate, other.delegate)
                    && Objects.equals(landBuilders, other.landBuilders);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("DMRoomBuilder [className=").append(className).append(", delegate=").append(delegate)
                    .append(", landBuilders=").append(landBuilders).append("]");
            return builder.toString();
        }

    }

    static DMRoom fromBuilder(DMRoomBuilder builder, Supplier<Land> landSupplier,
            Supplier<CommandChainHandler> successorSupplier, Supplier<Consumer<? super Room>> postRoomOperations,
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

    DMRoom(DMRoomBuilder builder, Supplier<Land> landSupplier, Supplier<CommandChainHandler> successorSupplier) {
        super(builder.delegate, landSupplier, successorSupplier);
        this.lands = new LandAtlas();
        this.users = new HashSet<>();
        this.commands = this.buildCommands();
        this.commands.keySet().removeAll(builder.getForbiddenCommandTypes());
    }

    public boolean addUser(User user) {
        if (this.filterCreatures(EnumSet.of(CreatureContainer.CreatureFilters.TYPE), null, null, null, null,
                DungeonMaster.class, null).size() < 2) {
            this.log(Level.INFO, () -> "Conditions met to create and add Player automatically");
            CreatureFactory factory = new CreatureFactory();
            return this.addNewPlayer(factory.buildPlayer(Player.getPlayerBuilder(user)));
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
        if (username == null) {
            return null;
        }
        for (User user : this.users) {
            if (username.equals(user.getUsername())) {
                return user;
            }
        }
        return null;
    }

    public User removeUser(String username) {
        if (username == null) {
            return null;
        }
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
        if (this.lands.size() <= 0) {
            return this.addCreature(player);
        }
        return this.lands.getFirstMember().addPlayer(player);
    }

    public void userExitSystem(User user) {
        for (Land land : this.lands.getAtlasMembers()) {
            if (land.removePlayer(user.getUserID()).isPresent()) {
                land.announce(UserLeftEvent.getBuilder().setUser(user).setBroacast().Build());
            }
        }
    }

    @Override
    public Collection<GameEventProcessor> getGameEventProcessors() {
        Collection<GameEventProcessor> messengers = new TreeSet<>(GameEventProcessor.getComparator());
        messengers.addAll(super.getGameEventProcessors());
        this.users.stream().filter(userThing -> userThing != null).forEach(userThing -> messengers.add(userThing));
        return messengers;
    }

    @Override
    public RoomAffectedEvent processEffectApplication(RoomEffect effect) {
        if (effect instanceof DMRoomEffect dmRoomEffect) {
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
                        ICreature.eventAccepter.accept(dmRoomEffect.creatureResponsible(), BadTargetSelectedEvent
                                .getBuilder().setBde(BadTargetOption.DNE).setBadTarget(name).Build());
                        return null;
                    }
                }
                Corpse corpse = (Corpse) maybeCorpse.get();
                this.removeItem(corpse);
                CreatureFactory factory = new CreatureFactory();
                PlayerBuildInfo playerBuilder = Player.getPlayerBuilder(user).setVocation(dmRoomEffect.getVocation())
                        .setCorpse(corpse);
                this.addNewPlayer(factory.buildPlayer(playerBuilder));
            }
        }
        return super.processEffectApplication(effect);
    }

    @Override
    public void acceptAreaVisitor(AreaVisitor visitor) {
        if (visitor != null) {
            visitor.visit(this);
        }
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
        public Reply visit(CommandContext ctx, SayMessage sayMessage) {
            if (sayMessage == null) {
                return ctx.failhandle();
            }
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
                        User.eventAccepter.accept(u, SpeakingEvent.getBuilder().setSayer(sayer)
                                .setMessage(sayMessage.getSequence()).setHearer(u).Build());
                        sent = true;
                        break;
                    }
                }
                if (sent) {
                    return ctx.handled();
                }
            }
            return super.visit(ctx, sayMessage);
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
