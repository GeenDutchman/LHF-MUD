package com.lhf.game.map;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;

import com.lhf.game.AffectableEntity;
import com.lhf.game.CreatureContainer;
import com.lhf.game.Game;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.Player;
import com.lhf.game.map.Area.AreaBuilder;
import com.lhf.game.map.Area.AreaBuilder.AreaBuilderID;
import com.lhf.game.map.Atlas.AtlasMappingItem;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.ITickEvent;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.events.GameEvent;
import com.lhf.server.client.user.UserID;

public interface Land extends CreatureContainer, CommandChainHandler, AffectableEntity<DungeonEffect> {
    public interface TraversalTester extends Serializable {
        public boolean test(ICreature creature, Directions direction, Area source, Area dest);
    }

    public final class AreaAtlas extends Atlas<Area, UUID> {

        protected AreaAtlas(Area first) {
            super(first);
        }

        @Override
        public UUID getIDForMemberType(Area member) {
            return member.getUuid();
        }

    }

    public interface LandBuilder extends Serializable {

        public final class AreaBuilderAtlas extends Atlas<AreaBuilder, AreaBuilderID> {

            protected AreaBuilderAtlas(AreaBuilder first) {
                super(first);
            }

            @Override
            public AreaBuilderID getIDForMemberType(AreaBuilder member) {
                return member.getAreaBuilderID();
            }

        }

        public abstract AreaBuilder getStartingAreaBuilder();

        public abstract AreaBuilderAtlas getAtlas();

        public abstract Land build(CommandChainHandler successor, Game game);

        public default Land build(Game game) {
            return this.build(game, game);
        }

    }

    public abstract AreaAtlas getAtlas();

    public abstract Area getStartingArea();

    public default Map<Directions, Atlas<Area, UUID>.TargetedTester> getAreaExits(Area area) {
        AreaAtlas atlas = this.getAtlas();
        Atlas<Area, UUID>.AtlasMappingItem ami = atlas.getAtlasMappingItem(area);
        return ami.getDirections();
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
