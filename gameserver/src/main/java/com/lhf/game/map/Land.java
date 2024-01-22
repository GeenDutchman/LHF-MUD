package com.lhf.game.map;

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
import java.util.logging.Level;

import com.google.common.base.Function;
import com.lhf.game.AffectableEntity;
import com.lhf.game.CreatureContainer;
import com.lhf.game.TickType;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.map.Area.AreaBuilder;
import com.lhf.game.map.Area.AreaBuilder.AreaBuilderID;
import com.lhf.game.map.Atlas.AtlasMappingItem;
import com.lhf.game.map.Atlas.TargetedTester;
import com.lhf.game.map.commandHandlers.LandSeeHandler;
import com.lhf.game.map.commandHandlers.LandShoutHandler;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.ITickEvent;
import com.lhf.messages.events.BadGoEvent;
import com.lhf.messages.events.BadGoEvent.BadGoType;
import com.lhf.messages.events.BadMessageEvent;
import com.lhf.messages.events.BadMessageEvent.BadMessageType;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.TickEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.GoMessage;
import com.lhf.server.client.user.UserID;

public interface Land extends CreatureContainer, CommandChainHandler, AffectableEntity<DungeonEffect> {

    public final class AreaAtlas extends Atlas<Area, UUID> {

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

    }

    public interface LandBuilder extends Serializable {

        public final static class LandBuilderID implements Comparable<LandBuilderID> {
            private final UUID id = UUID.randomUUID();

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

        }

        public abstract LandBuilderID getLandBuilderID();

        public abstract String getName();

        public final class AreaBuilderAtlas extends Atlas<AreaBuilder, AreaBuilderID> implements Serializable {

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

        }

        public abstract AreaBuilder getStartingAreaBuilder();

        public abstract AreaBuilderAtlas getAtlas();

        public default Map<AreaBuilderID, UUID> translateAtlas(Land builtLand, AIRunner aiRunner,
                StatblockManager statblockManager, ConversationManager conversationManager,
                boolean fallbackNoConversation,
                boolean fallbackDefaultStatblock) {

            final Function<AreaBuilder, Area> transformer = (builder) -> {
                return builder.build(builtLand, builtLand, aiRunner,
                        statblockManager, conversationManager, fallbackNoConversation, fallbackDefaultStatblock);
            };

            final AreaBuilderAtlas builderAtlas = this.getAtlas();
            if (builderAtlas == null) {
                return null;
            }
            return builderAtlas.translate(() -> builtLand.getAtlas(), transformer);
        }

        public default Land quickBuild(CommandChainHandler successor, AIRunner aiRunner) {
            return build(successor, aiRunner, null, null, true, true);
        }

        public abstract Land build(CommandChainHandler successor, AIRunner aiRunner, StatblockManager statblockManager,
                ConversationManager conversationManager,
                boolean fallbackNoConversation,
                boolean fallbackDefaultStatblock);

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
        return atlas.getAtlasMember(startingAreaUUID);
    }

    public default Set<Directions> getAreaExits(Area area) {
        try {
            AreaAtlas atlas = this.getAtlas();
            AtlasMappingItem<Area, UUID> ami = atlas.getAtlasMappingItem(area);
            return ami.getAvailableDirections();
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
        return this.getAtlas().getAtlasMembers().stream()
                .filter(area -> area != null && area.hasCreature(creature))
                .findFirst().orElseGet(() -> null);
    }

    public default Area getCreatureArea(String name) {
        return this.getAtlas().getAtlasMembers().stream()
                .filter(area -> area != null && area.hasCreature(name, null))
                .findFirst().orElseGet(() -> null);

    }

    public default Area getPlayerArea(UserID id) {
        return this.getAtlas().getAtlasMembers().stream()
                .filter(area -> area != null && area.getPlayer(id).isPresent())
                .findFirst().orElseGet(() -> null);

    }

    @Override
    public default Collection<ICreature> getCreatures() {
        Set<ICreature> creatures = new TreeSet<>();
        Area startingArea = this.getStartingArea();
        if (startingArea != null) {
            creatures.addAll(startingArea.getCreatures());
        }
        this.getAtlas().getAtlasMembers().stream()
                .filter(area -> area != null)
                .forEach(area -> creatures.addAll(area.getCreatures()));
        return Collections.unmodifiableSet(creatures);
    }

    public interface LandCommandHandler extends CommandHandler {

        static final EnumMap<AMessageType, CommandHandler> landCommandHandlers = new EnumMap<>(Map.of(
                AMessageType.GO, new LandGoHandler(),
                AMessageType.SEE, new LandSeeHandler(),
                AMessageType.SHOUT, new LandShoutHandler()));

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
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == this.getHandleType()) {
                final GoMessage goMessage = new GoMessage(cmd);
                final Land land = ctx.getLand();
                if (ctx.getCreature() == null) {
                    ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                            .setHelps(ctx.getHelps()).setCommand(cmd).Build());
                    return ctx.handled();
                }
                Directions toGo = goMessage.getDirection();
                if (ctx.getArea() == null) {
                    ctx.receive(BadGoEvent.getBuilder().setSubType(BadGoType.NO_ROOM).setAttempted(toGo).Build());
                    return ctx.handled();
                }
                Area presentRoom = ctx.getArea();
                final AtlasMappingItem<Area, UUID> mappingItem = land.getAtlas()
                        .getAtlasMappingItem(presentRoom.getUuid());
                if (mappingItem != null) {
                    Map<Directions, TargetedTester<UUID>> exits = mappingItem.getDirections();
                    if (exits == null || exits.size() == 0
                            || !exits.containsKey(toGo)
                            || exits.get(toGo) == null) {
                        ctx.receive(BadGoEvent.getBuilder().setSubType(BadGoType.DNE).setAttempted(toGo).Build());
                        return ctx.handled();
                    }
                    TargetedTester<UUID> doorway = exits.get(toGo);
                    final Area nextRoom = land.getAtlas().getAtlasMember(doorway.getTargetId());
                    if (nextRoom == null) {
                        ctx.receive(BadGoEvent.getBuilder().setSubType(BadGoType.DNE).setAttempted(toGo)
                                .setAvailable(exits.keySet()).Build());
                        return ctx.handled();
                    }
                    Doorway tester = doorway.getPredicate();
                    if (tester != null && !tester.testTraversal(ctx.getCreature(), toGo, presentRoom, presentRoom)) {
                        ctx.receive(BadGoEvent.getBuilder().setSubType(BadGoType.BLOCKED).setAttempted(toGo)
                                .setAvailable(exits.keySet()).Build());
                        return ctx.handled();
                    }

                    if (presentRoom.removeCreature(ctx.getCreature(), toGo)) {
                        ICreature.eventAccepter.accept(ctx.getCreature(),
                                TickEvent.getBuilder().setTickType(TickType.ROOM).Build());
                        nextRoom.addCreature(ctx.getCreature());
                        return ctx.handled();
                    }
                } else {
                    ctx.receive(BadGoEvent.getBuilder().setSubType(BadGoType.NO_ROOM)
                            .setAttempted(goMessage.getDirection()).Build());
                    return ctx.handled();
                }
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
            if (event instanceof ITickEvent tickEvent) {
                this.tick(tickEvent);
            }
            this.announceDirect(event, this.getGameEventProcessors());
        };
    }

    @Override
    public default String getStartTag() {
        return "<Land>";
    }

    @Override
    public default String getEndTag() {
        return "</Land>";
    }

    @Override
    public default String getColorTaggedName() {
        return this.getStartTag() + this.getName() + this.getEndTag();
    }

}
