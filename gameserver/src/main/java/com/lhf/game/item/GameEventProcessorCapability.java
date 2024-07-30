package com.lhf.game.item;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

import com.lhf.game.IExternalReference;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.dice.DiceDC;
import com.lhf.game.enums.Attributes;
import com.lhf.game.map.Area;
import com.lhf.game.map.RoomEffect;
import com.lhf.game.map.RoomEffectSource;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.GameEventTester;
import com.lhf.messages.events.SeeEvent.ABuilder;

public interface GameEventProcessorCapability extends ItemCapability, GameEventProcessor {

    public boolean isActivated();

    public GameEventProcessorCapability setActivated(boolean nextState);

    public Map<Attributes, DiceDC> getDisarmDifficulties();

    public List<IExternalReference<Area>> getExternalAreaReferences();

    public default Area getEventfulArea() {
        final List<IExternalReference<Area>> retrieved = this.getExternalAreaReferences();
        if (retrieved == null || retrieved.isEmpty()) {
            return null;
        }
        for (IExternalReference<Area> iExternalReference : retrieved) {
            if (iExternalReference != null) {
                return iExternalReference.getReference();
            }
        }
        return null;
    }

    /**
     * When a GameEventTester is triggered, then the associated set of effects on
     * the Area activate.
     * 
     * @return
     */
    public Map<GameEventTester, Set<RoomEffectSource>> getTriggeredAreaEffects();

    // and one for effects on this item itself

    @Override
    default boolean isStateful() {
        return true;
    }

    @Override
    default ItemCapabilityNames getCapabilityName() {
        return ItemCapabilityNames.EVENTFUL;
    }

    @Override
    default void describe(ABuilder<?> seeEventBuilder) {
        return;
    }

    public static GameEventProcessorCapability generateGameEventProcessorCapability() {
        return new Reactor();
    }

    public static final class Reactor implements GameEventProcessorCapability {
        private final GameEventProcessorID id = new GameEventProcessorID();
        private boolean activated;
        private final EnumMap<Attributes, DiceDC> disarmDifficulties;
        private final Area.AreaReference eventfulArea;
        private final LinkedHashMap<GameEventTester, Set<RoomEffectSource>> triggeredAreaEffects;

        public Reactor() {
            this.disarmDifficulties = null;
            this.eventfulArea = null;
            this.triggeredAreaEffects = null;
            this.activated = true;
        }

        @Override
        public Consumer<GameEvent> getAcceptHook() {
            return (event) -> {
                if (event == null) {
                    return;
                }
                if (eventfulArea == null) {
                    return;
                }
                final Area area = this.getEventfulArea();
                if (area == null) {
                    return;
                }
                final Map<GameEventTester, Set<RoomEffectSource>> triggers = this.getTriggeredAreaEffects();
                if (triggers == null) {
                    return;
                }
                for (final Entry<GameEventTester, Set<RoomEffectSource>> triggeredEffects : triggers.entrySet()) {
                    final GameEventTester tester = triggeredEffects.getKey();
                    if (tester == null) {
                        continue;
                    }
                    if (!tester.test(event)) {
                        continue;
                    }
                    final Set<RoomEffectSource> effects = triggeredEffects.getValue();
                    if (effects == null) {
                        continue;
                    }
                    for (final RoomEffectSource roomEffectSource : effects) {
                        // TODO: may need to re-think events and messaging
                        area.applyEffect(new RoomEffect(roomEffectSource, null, this));
                    }
                }
            };
        }

        @Override
        public void log(Level logLevel, String logMessage) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'log'");
        }

        @Override
        public void log(Level logLevel, Supplier<String> logMessageSupplier) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'log'");
        }

        @Override
        public GameEventProcessorID getEventProcessorID() {
            return this.id;
        }

        @Override
        public String getTagName() {
            return "ItemCapability";
        }

        @Override
        public String getSimpleContent() {
            return "Reactor";
        }

        @Override
        public boolean isActivated() {
            return this.activated;
        }

        @Override
        public Reactor setActivated(boolean nextState) {
            this.activated = nextState;
            return this;
        }

        @Override
        public Map<Attributes, DiceDC> getDisarmDifficulties() {
            return this.disarmDifficulties != null ? Collections.unmodifiableMap(this.disarmDifficulties) : Map.of();
        }

        @Override
        public List<IExternalReference<Area>> getExternalAreaReferences() {
            return this.eventfulArea != null ? List.of(this.eventfulArea) : List.of();
        }

        @Override
        public Map<GameEventTester, Set<RoomEffectSource>> getTriggeredAreaEffects() {
            return this.triggeredAreaEffects != null ? Collections.unmodifiableMap(this.triggeredAreaEffects)
                    : Map.of();
        }

    }

}
