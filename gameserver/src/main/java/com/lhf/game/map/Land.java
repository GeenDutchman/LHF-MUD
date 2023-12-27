package com.lhf.game.map;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
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
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.ITickEvent;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.events.GameEvent;
import com.lhf.server.client.user.UserID;

public interface Land extends CreatureContainer, CommandChainHandler, AffectableEntity<DungeonEffect> {

    public interface DirectionalLinks {
        public abstract UUID getID();

        public abstract Map<Directions, Doorway> getExits();
    }

    public interface AreaDirectionalLinks extends DirectionalLinks {
        public abstract Area getArea();

        @Override
        default UUID getID() {
            final Area foundArea = this.getArea();
            if (foundArea == null) {
                throw new IllegalStateException("An AreaDirectionalLink cannot have a null area!");
            }
            return foundArea.getUuid();
        }

    }

    public interface LandBuilder extends Serializable {
        public interface AreaBuilderDirectionalLinks extends Serializable, DirectionalLinks {
            public abstract AreaBuilder getAreaBuilder();

            @Override
            default UUID getID() {
                final AreaBuilder foundArea = this.getAreaBuilder();
                if (foundArea == null) {
                    throw new IllegalStateException("An AreaDirectionalLink cannot have a null area!");
                }
                return foundArea.getAreaBuilderID().getId();
            }

        }

        public abstract AreaBuilder getStartingAreaBuilder();

        public abstract Map<AreaBuilderID, ? extends AreaBuilderDirectionalLinks> getAtlas();

        public abstract Land build(CommandChainHandler successor, Game game);

        public default Land build(Game game) {
            return this.build(game, game);
        }

    }

    public abstract Map<UUID, AreaDirectionalLinks> getAtlas();

    public abstract Area getStartingArea();

    public default AreaDirectionalLinks getAreaDirectionalLinks(Area area) {
        Map<UUID, AreaDirectionalLinks> atlas = this.getAtlas();
        if (atlas == null || atlas.size() == 0 || !atlas.containsKey(area.getUuid())) {
            return null;
        }
        return atlas.get(area.getUuid());
    }

    public default Map<Directions, Doorway> getAreaExits(Area area) {
        AreaDirectionalLinks links = this.getAreaDirectionalLinks(area);
        if (links == null) {
            return null;
        }
        return links.getExits();
    }

    public default Area getCreatureArea(ICreature creature) {
        for (AreaDirectionalLinks adl : this.getAtlas().values()) {
            Area adlArea = adl.getArea();
            if (adlArea != null) {
                if (adlArea.hasCreature(creature)) {
                    return adlArea;
                }
            }
        }
        return null;
    }

    public default Area getCreatureArea(String name) {
        for (AreaDirectionalLinks adl : this.getAtlas().values()) {
            Area adlArea = adl.getArea();
            if (adlArea != null) {
                if (adlArea.hasCreature(name, null)) {
                    return adlArea;
                }
            }
        }
        return null;
    }

    public default Area getPlayerArea(UserID id) {
        for (AreaDirectionalLinks rAndD : this.getAtlas().values()) {
            Area randArea = rAndD.getArea();
            if (randArea != null) {
                Optional<Player> found = randArea.getPlayer(id);
                if (found.isPresent()) {
                    return randArea;
                }
            }
        }
        return null;
    }

    @Override
    public default Collection<ICreature> getCreatures() {
        Set<ICreature> creatures = new TreeSet<>();
        Area startingArea = this.getStartingArea();
        if (startingArea != null) {
            creatures.addAll(startingArea.getCreatures());
        }
        for (AreaDirectionalLinks rAndD : this.getAtlas().values()) {
            Area area = rAndD.getArea();
            if (area != null) {
                creatures.addAll(area.getCreatures());
            }
        }
        return Collections.unmodifiableSet(creatures);
    }

    @Override
    public default Collection<GameEventProcessor> getGameEventProcessors() {
        Set<GameEventProcessor> messengers = new TreeSet<>(GameEventProcessor.getComparator());
        Area startingArea = this.getStartingArea();
        if (startingArea != null) {
            messengers.add(startingArea);
        }

        Map<UUID, AreaDirectionalLinks> atlas = this.getAtlas();
        if (atlas != null) {
            atlas.values().stream().filter(rAndD -> rAndD != null && rAndD.getArea() != null)
                    .forEach(rAndD -> messengers.add(rAndD.getArea()));
        }

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
