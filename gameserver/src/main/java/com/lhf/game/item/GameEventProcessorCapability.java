package com.lhf.game.item;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.lhf.game.IExternalReference;
import com.lhf.game.creature.ICreature;
import com.lhf.game.dice.DiceDC;
import com.lhf.game.enums.Attributes;
import com.lhf.game.map.Area;
import com.lhf.game.map.RoomEffect;
import com.lhf.game.map.RoomEffectSource;
import com.lhf.messages.CommandContext;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.GameEventTester;
import com.lhf.messages.events.SeeEvent.ABuilder;

public interface GameEventProcessorCapability extends ItemCapability, GameEventProcessor {

    public boolean isArmed();

    public GameEventProcessorCapability setArmed(boolean nextState);

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
        // TODO: describe if activated
        return;
    }

    public static GameEventProcessorCapability generateGameEventProcessorCapability() {
        return new Reactor(null);
    }

    public static boolean changeArming(CommandContext ctx, IItem myItem) {
        if (ctx == null) {
            return false;
        }
        // TODO: make a command for this?
        final ICreature creature = ctx.getCreature();
        if (creature == null) {
            return false; // error message
        }
        final GameEventProcessorCapability capability = myItem.getGameEventProcessorCapability();
        if (capability == null) {
            return false;
        }
        final Map<Attributes, DiceDC> difficulties = capability.getDisarmDifficulties();
        if (difficulties != null) {
            final Attributes attr = creature.getHighestAttributeBonus(difficulties.keySet());
            if (attr == null) {
                return false;
            }
            if (creature.check(attr).getRoll() < difficulties.get(attr).rollDice().getRoll()) {
                return false;
            }
            capability.setArmed(!capability.isArmed());
            return true;
        }
        return false;
    }

    public static final class Reactor implements GameEventProcessorCapability {
        private final GameEventProcessorID id = new GameEventProcessorID();
        private boolean armed;
        private final EnumMap<Attributes, DiceDC> disarmDifficulties;
        private final Area.AreaReference eventfulArea;
        private final LinkedHashMap<GameEventTester, Set<RoomEffectSource>> triggeredAreaEffects;

        public static class Builder {
            private boolean armed = true;
            private EnumMap<Attributes, DiceDC> disarmDifficulties;
            private Area.AreaReference eventfulArea;
            private LinkedHashMap<GameEventTester, Set<RoomEffectSource>> triggeredAreaEffects;

            public boolean isArmed() {
                return armed;
            }

            public Builder setArmed(boolean armed) {
                this.armed = armed;
                return this;
            }

            public EnumMap<Attributes, DiceDC> getDisarmDifficulties() {
                return disarmDifficulties;
            }

            public Builder setDisarmDifficulties(EnumMap<Attributes, DiceDC> disarmDifficulties) {
                this.disarmDifficulties = disarmDifficulties;
                return this;
            }

            public Builder addDisarmDifficulty(Attributes attr, DiceDC dc) {
                if (attr != null && dc != null) {
                    if (this.disarmDifficulties == null) {
                        this.disarmDifficulties = new EnumMap<>(Attributes.class);
                    }
                    this.disarmDifficulties.put(attr, dc);
                }
                return this;
            }

            public Builder adjustDisarmDifficulties(Consumer<Map<Attributes, DiceDC>> adjustor) {
                if (adjustor != null) {
                    if (this.disarmDifficulties == null) {
                        this.disarmDifficulties = new EnumMap<>(Attributes.class);
                    }
                    adjustor.accept(disarmDifficulties);
                }
                return this;
            }

            public Area.AreaReference getEventfulArea() {
                return eventfulArea;
            }

            public Builder setEventfulArea(Area.AreaReference eventfulArea) {
                this.eventfulArea = eventfulArea;
                return this;
            }

            public LinkedHashMap<GameEventTester, Set<RoomEffectSource>> getTriggeredAreaEffects() {
                return triggeredAreaEffects;
            }

            public Builder setTriggeredAreaEffects(
                    LinkedHashMap<GameEventTester, Set<RoomEffectSource>> triggeredAreaEffects) {
                this.triggeredAreaEffects = triggeredAreaEffects;
                return this;
            }

            public Builder addTriggeredAreaEffects(GameEventTester tester, Set<RoomEffectSource> effects) {
                if (tester != null && effects != null) {
                    if (this.triggeredAreaEffects == null) {
                        this.triggeredAreaEffects = new LinkedHashMap<>();
                    }
                    this.triggeredAreaEffects.put(tester, effects);
                }
                return this;
            }

            public Builder adjustTriggeredAreaEffects(Consumer<Map<GameEventTester, Set<RoomEffectSource>>> adjustor) {
                if (adjustor != null) {
                    if (this.triggeredAreaEffects == null) {
                        this.triggeredAreaEffects = new LinkedHashMap<>();
                    }
                    adjustor.accept(triggeredAreaEffects);
                }
                return this;
            }

            public Reactor build() {
                return new Reactor(this);
            }

        }

        private Reactor(Builder builder) {
            if (builder == null) {
                this.disarmDifficulties = null;
                this.eventfulArea = null;
                this.triggeredAreaEffects = null;
                this.armed = true;
            } else {
                this.disarmDifficulties = builder.disarmDifficulties != null ? new EnumMap<>(builder.disarmDifficulties)
                        : new EnumMap<>(Attributes.class);
                this.eventfulArea = Area.AreaReference.copy(builder.getEventfulArea());
                this.triggeredAreaEffects = builder.triggeredAreaEffects != null
                        ? builder.triggeredAreaEffects.entrySet().stream()
                                .collect(Collectors.toMap(entry -> entry.getKey(),
                                        entry -> entry.getValue() != null ? new LinkedHashSet<>(entry.getValue())
                                                : Set.of(),
                                        (a, b) -> b, () -> new LinkedHashMap<GameEventTester, Set<RoomEffectSource>>()))
                        : new LinkedHashMap<>();
                this.armed = builder.isArmed();
            }
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
        public boolean isArmed() {
            return this.armed;
        }

        @Override
        public Reactor setArmed(boolean nextState) {
            this.armed = nextState;
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
