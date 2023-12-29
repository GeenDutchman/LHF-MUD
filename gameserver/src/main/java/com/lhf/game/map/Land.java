package com.lhf.game.map;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

import com.google.common.base.Function;
import com.lhf.game.AffectableEntity;
import com.lhf.game.CreatureContainer;
import com.lhf.game.Game;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.map.Area.AreaBuilder;
import com.lhf.game.map.Area.AreaBuilder.AreaBuilderID;
import com.lhf.game.map.Atlas.AtlasMappingItem;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.ITickEvent;
import com.lhf.messages.events.GameEvent;
import com.lhf.server.client.user.UserID;

public interface Land extends CreatureContainer, CommandChainHandler, AffectableEntity<DungeonEffect> {
    public interface TraversalTester extends Serializable {
        public boolean testTraversal(ICreature creature, Directions direction, Area source, Area dest);
    }

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

        @FunctionalInterface
        public static interface PostBuildOperations<L extends Land> {
            public abstract void accept(L land) throws FileNotFoundException;

            public default PostBuildOperations<L> andThen(PostBuildOperations<? super L> after) {
                Objects.requireNonNull(after);
                return (t) -> {
                    this.accept(t);
                    after.accept(t);
                };
            }
        }

        public class LandBuilderID implements Comparable<LandBuilderID> {
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

        public default Map<AreaBuilderID, UUID> quickTranslateAtlas(Land builtLand, AIRunner aiRunner) {
            final Function<AreaBuilder, Area> transformer = (builder) -> {
                return builder.quickBuild(builtLand, builtLand, aiRunner);
            };

            final AreaBuilderAtlas builderAtlas = this.getAtlas();
            if (builderAtlas == null) {
                return null;
            }
            return builderAtlas.translate(() -> builtLand.getAtlas(), transformer);
        }

        public default Map<AreaBuilderID, UUID> translateAtlas(Land builtLand, AIRunner aiRunner,
                StatblockManager statblockManager, ConversationManager conversationManager) {

            final Function<AreaBuilder, Area> transformer = (builder) -> {
                try {
                    return builder.build(builtLand, aiRunner,
                            statblockManager, conversationManager);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    throw new IllegalStateException("Cannot find necessary file!", e);
                }
            };

            final AreaBuilderAtlas builderAtlas = this.getAtlas();
            if (builderAtlas == null) {
                return null;
            }
            return builderAtlas.translate(() -> builtLand.getAtlas(), transformer);
        }

        public abstract Land quickBuild(CommandChainHandler successor, AIRunner aiRunner);

        public abstract Land build(CommandChainHandler successor, AIRunner aiRunner, StatblockManager statblockManager,
                ConversationManager conversationManager) throws FileNotFoundException;

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
