package com.lhf.game.map;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.lhf.game.AffectableEntity;
import com.lhf.game.Atlas;
import com.lhf.game.Atlas.AtlasException;
import com.lhf.game.Atlas.AtlasFunction;
import com.lhf.game.CreatureContainer;
import com.lhf.game.TickType;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.map.Area.AreaBuilder;
import com.lhf.game.map.Area.AreaBuilder.AreaBuilderID;
import com.lhf.game.map.commandHandlers.LandSeeHandler;
import com.lhf.game.map.commandHandlers.LandShoutHandler;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.events.BadGoEvent;
import com.lhf.messages.events.BadGoEvent.BadGoType;
import com.lhf.messages.events.BadMessageEvent;
import com.lhf.messages.events.BadMessageEvent.BadMessageType;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.TickEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.GoMessage;
import com.lhf.server.client.user.UserID;
import com.lhf.server.interfaces.NotNull;

public interface Land extends CreatureContainer, CommandChainHandler, AffectableEntity<DungeonEffect> {

    public final class AreaAtlas extends Atlas<Area, UUID, Directions, Doorway> {

        protected AreaAtlas() {
            super();
        }

        @Override
        public UUID getIDForMemberType(Area member) {
            return member.getUuid();
        }

        @Override
        public String getNameForMemberType(Area member) {
            return member.getName();
        }

        @Override
        public Directions translateLinkToOpposite(Directions link) {
            if (link == null) {
                return null;
            }
            return link.opposite();
        }

        @Override
        public Atlas<Area, UUID, Directions, Doorway>.AtlasToMermaidWriter generateMermaidWriter(String indent) {
            return this.new AtlasToMermaidWriter(indent) {

                @Override
                protected String displayID(UUID id) {
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
                protected String displayMemberNote(Area member) {
                    return "";
                }

            };
        }
    }

    public interface LandBuilder extends Serializable {

        public final static class LandBuilderID implements Comparable<LandBuilderID> {
            private final UUID id;

            public LandBuilderID() {
                id = UUID.randomUUID();
            }

            protected LandBuilderID(@NotNull UUID id) {
                this.id = id;
            }

            public UUID getId() {
                return id;
            }

            @Override
            public int hashCode() {
                return Objects.hash(id);
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (!(obj instanceof LandBuilderID))
                    return false;
                LandBuilderID other = (LandBuilderID) obj;
                return Objects.equals(id, other.id);
            }

            @Override
            public int compareTo(LandBuilderID arg0) {
                return this.id.compareTo(arg0.id);
            }

            @Override
            public String toString() {
                return this.id.toString();
            }

            public static class IDTypeAdapter extends TypeAdapter<LandBuilderID> {

                @Override
                public void write(JsonWriter out, LandBuilderID value) throws IOException {
                    out.value(value.getId().toString());
                }

                @Override
                public LandBuilderID read(JsonReader in) throws IOException {
                    final String asStr = in.nextString();
                    return new LandBuilderID(UUID.fromString(asStr));
                }

            }

        }

        public abstract LandBuilderID getLandBuilderID();

        public abstract String getName();

        public final class AreaBuilderAtlas extends Atlas<AreaBuilder, AreaBuilderID, Directions, Doorway>
                implements Serializable {

            protected AreaBuilderAtlas() {
                super();
            }

            @Override
            public AreaBuilderID getIDForMemberType(AreaBuilder member) {
                return member.getAreaBuilderID();
            }

            @Override
            public String getNameForMemberType(AreaBuilder member) {
                return member.getName();
            }

            @Override
            public Directions translateLinkToOpposite(Directions link) {
                if (link == null) {
                    return null;
                }
                return link.opposite();
            }

            @Override
            public Atlas<AreaBuilder, AreaBuilderID, Directions, Doorway>.AtlasToMermaidWriter generateMermaidWriter(
                    String indent) {
                return new AtlasToMermaidWriter(indent) {

                    @Override
                    protected String displayID(AreaBuilderID id) {
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
                    protected String displayMemberNote(AreaBuilder member) {
                        return "";
                    }

                };
            }
        }

        public abstract AreaBuilder getStartingAreaBuilder();

        public abstract AreaBuilderAtlas getAtlas();

        public default Map<AreaBuilderID, UUID> translateAtlas(Land builtLand, AIRunner aiRunner,
                ConversationManager conversationManager, boolean fallbackNoConversation) throws AtlasException {

            final Supplier<Atlas<Area, UUID, Directions, Doorway>> starter = () -> builtLand.getAtlas();

            final AtlasFunction<AreaBuilder, Area> transformer = (builder) -> {
                return builder.build(builtLand, builtLand, aiRunner, conversationManager, fallbackNoConversation);
            };

            final AtlasFunction<Directions, Directions> linkTransforer = (dir) -> dir;

            final AtlasFunction<Doorway, Doorway> traversalTransformer = (doorway) -> doorway;

            final AreaBuilderAtlas builderAtlas = this.getAtlas();
            if (builderAtlas == null) {
                return null;
            }
            return builderAtlas.<Area, UUID, Directions, Doorway>translateToSuppliedAtlas(starter, transformer,
                    linkTransforer, traversalTransformer);
        }

        public default Land quickBuild(CommandChainHandler successor, AIRunner aiRunner) throws AtlasException {
            return build(successor, aiRunner, null, true);
        }

        public abstract Land build(CommandChainHandler successor, AIRunner aiRunner,
                ConversationManager conversationManager, boolean fallbackNoConversation) throws AtlasException;

    }

    public abstract AreaAtlas getAtlas();

    public abstract void setStartingAreaUUID(UUID areaID);

    public abstract UUID getStartingAreaUUID();

    public default Area getStartingArea() {
        AreaAtlas atlas = this.getAtlas();
        if (atlas == null) {
            return null;
        }
        UUID startingAreaUUID = this.getStartingAreaUUID();
        if (startingAreaUUID == null) {
            Area firstMember = atlas.getFirstMember();
            if (firstMember != null) {
                this.setStartingAreaUUID(firstMember.getUuid()); // cache that value
            }
            return firstMember;
        }
        return atlas.getAtlasMemberOrNull(startingAreaUUID);
    }

    public default Set<Directions> getAreaExits(Area area) {
        try {
            AreaAtlas atlas = this.getAtlas();
            return atlas.getLinksForMember(area.getUuid());
        } catch (NullPointerException e) {
            this.log(Level.WARNING, String.format("Atlas error for getting exits: %s", e));
            return Set.of();
        }
    }

    public default Optional<Area> getAreaByName(String name) {
        AreaAtlas atlas = this.getAtlas();
        return atlas.getAtlasMembers().stream().filter(area -> area != null && area.getName().equals(name)).findFirst();
    }

    public default Area getCreatureArea(ICreature creature) {
        return this.getAtlas().getAtlasMembers().stream().filter(area -> area != null && area.hasCreature(creature))
                .findFirst().orElseGet(() -> null);
    }

    public default Area getCreatureArea(String name) {
        return this.getAtlas().getAtlasMembers().stream().filter(area -> area != null && area.hasCreature(name, null))
                .findFirst().orElseGet(() -> null);

    }

    public default Area getPlayerArea(UserID id) {
        return this.getAtlas().getAtlasMembers().stream().filter(area -> area != null && area.getPlayer(id).isPresent())
                .findFirst().orElseGet(() -> null);

    }

    @Override
    public default Collection<ICreature> getCreatures() {
        Set<ICreature> creatures = new TreeSet<>();
        Area startingArea = this.getStartingArea();
        if (startingArea != null) {
            creatures.addAll(startingArea.getCreatures());
        }
        this.getAtlas().getAtlasMembers().stream().filter(area -> area != null)
                .forEach(area -> creatures.addAll(area.getCreatures()));
        return Collections.unmodifiableSet(creatures);
    }

    public interface LandCommandHandler extends CommandHandler {

        static final EnumMap<AMessageType, CommandHandler> landCommandHandlers = new EnumMap<>(
                Map.of(AMessageType.GO, new LandGoHandler(), AMessageType.SEE, new LandSeeHandler(), AMessageType.SHOUT,
                        new LandShoutHandler()));

        @Override
        public default boolean isEnabled(CommandContext ctx) {
            if (ctx == null) {
                return false;
            }
            final ICreature creature = ctx.getCreature();
            if (creature == null || !creature.isAlive()) {
                return false;
            }
            final Area area = ctx.getArea();
            if (area == null) {
                return false;
            }
            final Land land = ctx.getLand();
            return land != null;
        }

        @Override
        public default CommandChainHandler getChainHandler(CommandContext ctx) {
            return ctx.getLand();
        }
    }

    public static class LandGoHandler implements LandCommandHandler {
        private final static String helpString = "\"go [direction]\" Move in the desired direction, if that direction exists.  Like \"go east\"";

        @Override
        public AMessageType getHandleType() {
            return AMessageType.GO;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(LandGoHandler.helpString);
        }

        @Override
        public Reply visit(CommandContext ctx, GoMessage command) {
            if (command == null) {
                return ctx.failhandle();
            }
            final Land land = ctx.getLand();
            if (ctx.getCreature() == null) {
                ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                        .setHelps(ctx.getHelps()).setCommand(command).Build());
                return ctx.handled();
            }
            Directions toGo = command.getDirection();
            if (ctx.getArea() == null) {
                ctx.receive(BadGoEvent.getBuilder().setSubType(BadGoType.NO_ROOM).setAttempted(toGo).Build());
                return ctx.handled();
            }
            Area presentRoom = ctx.getArea();

            Set<Directions> exits = land.getAtlas().getLinksForMember(presentRoom.getUuid());
            if (exits == null || exits.size() == 0 || !exits.contains(toGo)) {
                ctx.receive(BadGoEvent.getBuilder().setSubType(BadGoType.DNE).setAttempted(toGo).Build());
                return ctx.handled();
            }
            UUID nextRoomID = land.getAtlas().getTargetFromMemberOrNull(presentRoom.getUuid(), toGo);
            final Area nextRoom = land.getAtlas().getAtlasMemberOrNull(nextRoomID);
            if (nextRoom == null) {
                ctx.receive(BadGoEvent.getBuilder().setSubType(BadGoType.NO_ROOM).setAttempted(command.getDirection())
                        .Build());
                return ctx.handled();
            }
            Doorway tester = land.getAtlas().getTraversalTestFromMemberOrNull(presentRoom.getUuid(), toGo);
            if (tester != null && !tester.testTraversal(ctx.getCreature(), toGo, presentRoom, presentRoom)) {
                ctx.receive(BadGoEvent.getBuilder().setSubType(BadGoType.BLOCKED).setAttempted(toGo).setAvailable(exits)
                        .Build());
                return ctx.handled();
            }

            if (presentRoom.removeCreature(ctx.getCreature(), toGo)) {
                ICreature.eventAccepter.accept(ctx.getCreature(),
                        TickEvent.getBuilder().setTickType(TickType.ROOM).Build());
                nextRoom.addCreature(ctx.getCreature());
                return ctx.handled();
            }
            return ctx.failhandle();
        }

    }

    @Override
    public default Collection<GameEventProcessor> getGameEventProcessors() {
        Set<GameEventProcessor> messengers = new TreeSet<>(GameEventProcessor.getComparator());
        Area startingArea = this.getStartingArea();
        if (startingArea != null) {
            messengers.add(startingArea);
        }

        this.getAtlas().getAtlasMembers().stream().filter(area -> area != null).forEach(area -> messengers.add(area));

        return Collections.unmodifiableCollection(messengers);
    }

    @Override
    public default Consumer<GameEvent> getAcceptHook() {
        return (event) -> {
            if (event == null) {
                return;
            }

            this.announceDirect(event, this.getGameEventProcessors());
            this.tick(event);
        };
    }

    @Override
    default String getTagName() {
        return "Land";
    }

    @Override
    default String getSimpleContent() {
        return this.getName();
    }

}
