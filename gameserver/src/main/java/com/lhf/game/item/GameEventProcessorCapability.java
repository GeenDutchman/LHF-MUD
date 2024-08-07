package com.lhf.game.item;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.lhf.game.IExternalReference;
import com.lhf.game.creature.CreatureEffectSource;
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

    public Set<CreatureEffectSource> getOnDisarmFailure();

    public Set<CreatureEffectSource> getOnDisarmSuccess();

    public Set<CreatureEffectSource> getOnArmingFailure();

    public Set<CreatureEffectSource> getOnArmingSuccess();

    public Map<Attributes, DiceDC> getDisarmDifficulties();

    public Map<Attributes, DiceDC> getArmingDifficulties();

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

    public static enum Delta implements Consumer<GameEventProcessorCapability> {
        NOOP {
            @Override
            public void accept(GameEventProcessorCapability arg0) {
                // does nothing
            }
        },
        TOGGLE {
            @Override
            public void accept(GameEventProcessorCapability arg0) {
                if (arg0 != null) {
                    arg0.setArmed(!arg0.isArmed());
                }
            }
        },
        ARM {
            @Override
            public void accept(GameEventProcessorCapability arg0) {
                if (arg0 != null) {
                    arg0.setArmed(true);
                }
            }
        },
        DISARM {
            @Override
            public void accept(GameEventProcessorCapability arg0) {
                if (arg0 != null) {
                    arg0.setArmed(false);
                }
            }
        };

        @Override
        public abstract void accept(GameEventProcessorCapability arg0);

        public Delta invert() {
            switch (this) {
            case ARM:
                return DISARM;
            case DISARM:
                return ARM;
            case NOOP:
                return NOOP;
            case TOGGLE:
                return TOGGLE;
            default:
                return NOOP;

            }
        }

    }

    public default void acceptDelta(Delta delta) {
        if (delta != null) {
            delta.accept(this);
        }
    }

    public static final class Reactor implements GameEventProcessorCapability {
        private final GameEventProcessorID id = new GameEventProcessorID();
        private boolean armed;
        private final EnumMap<Attributes, DiceDC> disarmDifficulties;
        private final EnumMap<Attributes, DiceDC> armingDifficulties;
        private final Set<CreatureEffectSource> onDisarmFailure;
        private final Set<CreatureEffectSource> onDisarmSuccess;
        private final Set<CreatureEffectSource> onArmingFailure;
        private final Set<CreatureEffectSource> onArmingSuccess;
        private final Area.AreaReference eventfulArea;
        private final LinkedHashMap<GameEventTester, Set<RoomEffectSource>> triggeredAreaEffects;

        public static class Builder {
            private boolean armed = true;
            private EnumMap<Attributes, DiceDC> disarmDifficulties;
            private EnumMap<Attributes, DiceDC> armingDifficulties;
            private Set<CreatureEffectSource> onDisarmFailure;
            private Set<CreatureEffectSource> onDisarmSuccess;
            private Set<CreatureEffectSource> onArmingFailure;
            private Set<CreatureEffectSource> onArmingSuccess;
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

            public EnumMap<Attributes, DiceDC> getArmingDifficulties() {
                return armingDifficulties;
            }

            public Builder setArmingDifficulties(EnumMap<Attributes, DiceDC> armingDifficulties) {
                this.armingDifficulties = armingDifficulties;
                return this;
            }

            public Builder addArmingDifficulty(Attributes attr, DiceDC dc) {
                if (attr != null && dc != null) {
                    if (this.armingDifficulties == null) {
                        this.armingDifficulties = new EnumMap<>(Attributes.class);
                    }
                    this.armingDifficulties.put(attr, dc);
                }
                return this;
            }

            public Builder adjustArmingDifficulties(
                    Function<EnumMap<Attributes, DiceDC>, EnumMap<Attributes, DiceDC>> adjustor) {
                if (adjustor != null) {
                    if (this.armingDifficulties == null) {
                        this.armingDifficulties = new EnumMap<>(Attributes.class);
                    }
                    this.armingDifficulties = adjustor.apply(armingDifficulties);
                }
                return this;
            }

            public Set<CreatureEffectSource> getOnDisarmFailure() {
                return onDisarmFailure;
            }

            public Builder setOnDisarmFailure(Set<CreatureEffectSource> onDisarmFailure) {
                this.onDisarmFailure = onDisarmFailure;
                return this;
            }

            public Builder addDisarmFailure(CreatureEffectSource source) {
                if (source != null) {
                    if (this.onDisarmFailure == null) {
                        this.onDisarmFailure = new LinkedHashSet<>();
                    }
                    this.onDisarmFailure.add(source);
                }
                return this;
            }

            public Builder adjustDisarmFailure(
                    Function<Set<CreatureEffectSource>, Set<CreatureEffectSource>> adjustor) {
                if (adjustor != null) {
                    if (this.onDisarmFailure == null) {
                        this.onDisarmFailure = new LinkedHashSet<>();
                    }
                    this.onDisarmFailure = adjustor.apply(onDisarmFailure);
                }
                return this;
            }

            public Set<CreatureEffectSource> getOnDisarmSuccess() {
                return onDisarmSuccess;
            }

            public Builder setOnDisarmSuccess(Set<CreatureEffectSource> onDisarmSuccess) {
                this.onDisarmSuccess = onDisarmSuccess;
                return this;
            }

            public Builder addDisarmSuccess(CreatureEffectSource source) {
                if (source != null) {
                    if (this.onDisarmSuccess == null) {
                        this.onDisarmSuccess = new LinkedHashSet<>();
                    }
                    this.onDisarmSuccess.add(source);
                }
                return this;
            }

            public Builder adjustDisarmSuccess(
                    Function<Set<CreatureEffectSource>, Set<CreatureEffectSource>> adjustor) {
                if (adjustor != null) {
                    if (this.onDisarmSuccess == null) {
                        this.onDisarmSuccess = new LinkedHashSet<>();
                    }
                    this.onDisarmSuccess = adjustor.apply(onDisarmSuccess);
                }
                return this;
            }

            public Set<CreatureEffectSource> getOnArmingFailure() {
                return onArmingFailure;
            }

            public Builder setOnArmingFailure(Set<CreatureEffectSource> onArmingFailure) {
                this.onArmingFailure = onArmingFailure;
                return this;
            }

            public Builder addArmingFailure(CreatureEffectSource source) {
                if (source != null) {
                    if (this.onArmingFailure == null) {
                        this.onArmingFailure = new LinkedHashSet<>();
                    }
                    this.onArmingFailure.add(source);
                }
                return this;
            }

            public Builder adjustArmingFailure(
                    Function<Set<CreatureEffectSource>, Set<CreatureEffectSource>> adjustor) {
                if (adjustor != null) {
                    if (this.onArmingFailure == null) {
                        this.onArmingFailure = new LinkedHashSet<>();
                    }
                    this.onArmingFailure = adjustor.apply(onArmingFailure);
                }
                return this;
            }

            public Set<CreatureEffectSource> getOnArmingSuccess() {
                return onArmingSuccess;
            }

            public Builder setOnArmingSuccess(Set<CreatureEffectSource> onArmingSuccess) {
                this.onArmingSuccess = onArmingSuccess;
                return this;
            }

            public Builder addArmingSuccess(CreatureEffectSource source) {
                if (source != null) {
                    if (this.onArmingSuccess == null) {
                        this.onArmingSuccess = new LinkedHashSet<>();
                    }
                    this.onArmingSuccess.add(source);
                }
                return this;
            }

            public Builder adjustArmingSuccess(
                    Function<Set<CreatureEffectSource>, Set<CreatureEffectSource>> adjustor) {
                if (adjustor != null) {
                    if (this.onArmingSuccess == null) {
                        this.onArmingSuccess = new LinkedHashSet<>();
                    }
                    this.onArmingSuccess = adjustor.apply(onArmingSuccess);
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

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append("Builder [armed=").append(armed).append(", disarmDifficulties=")
                        .append(disarmDifficulties).append(", armingDifficulties=").append(armingDifficulties)
                        .append(", onDisarmFailure=").append(onDisarmFailure).append(", onDisarmSuccess=")
                        .append(onDisarmSuccess).append(", onArmingFailure=").append(onArmingFailure)
                        .append(", onArmingSuccess=").append(onArmingSuccess).append(", eventfulArea=")
                        .append(eventfulArea).append(", triggeredAreaEffects=").append(triggeredAreaEffects)
                        .append("]");
                return builder.toString();
            }

            public Reactor build() {
                return new Reactor(this);
            }

        }

        private Reactor(Builder builder) {
            if (builder == null) {
                this.armingDifficulties = null;
                this.disarmDifficulties = null;
                this.eventfulArea = null;
                this.triggeredAreaEffects = null;
                this.armed = false;
                this.onArmingFailure = null;
                this.onArmingSuccess = null;
                this.onDisarmFailure = null;
                this.onDisarmSuccess = null;
            } else {
                this.armingDifficulties = builder.armingDifficulties != null ? new EnumMap<>(builder.armingDifficulties)
                        : new EnumMap<>(Attributes.class);
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
                this.onArmingFailure = builder.onArmingFailure != null ? new LinkedHashSet<>(builder.onArmingFailure)
                        : Set.of();
                this.onArmingSuccess = builder.onArmingSuccess != null ? new LinkedHashSet<>(builder.onArmingSuccess)
                        : Set.of();
                this.onDisarmFailure = builder.onDisarmFailure != null ? new LinkedHashSet<>(builder.onDisarmFailure)
                        : Set.of();
                this.onDisarmSuccess = builder.onDisarmSuccess != null ? new LinkedHashSet<>(builder.onDisarmSuccess)
                        : Set.of();

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

        public GameEventProcessorID getId() {
            return id;
        }

        public Map<Attributes, DiceDC> getArmingDifficulties() {
            return armingDifficulties != null ? Collections.unmodifiableMap(this.armingDifficulties) : Map.of();
        }

        public Set<CreatureEffectSource> getOnDisarmFailure() {
            return onDisarmFailure;
        }

        public Set<CreatureEffectSource> getOnDisarmSuccess() {
            return onDisarmSuccess;
        }

        public Set<CreatureEffectSource> getOnArmingFailure() {
            return onArmingFailure;
        }

        public Set<CreatureEffectSource> getOnArmingSuccess() {
            return onArmingSuccess;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, armed, disarmDifficulties, armingDifficulties, onDisarmFailure, onDisarmSuccess,
                    onArmingFailure, onArmingSuccess, eventfulArea, triggeredAreaEffects);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Reactor))
                return false;
            Reactor other = (Reactor) obj;
            return Objects.equals(id, other.id) && armed == other.armed
                    && Objects.equals(disarmDifficulties, other.disarmDifficulties)
                    && Objects.equals(armingDifficulties, other.armingDifficulties)
                    && Objects.equals(onDisarmFailure, other.onDisarmFailure)
                    && Objects.equals(onDisarmSuccess, other.onDisarmSuccess)
                    && Objects.equals(onArmingFailure, other.onArmingFailure)
                    && Objects.equals(onArmingSuccess, other.onArmingSuccess)
                    && Objects.equals(eventfulArea, other.eventfulArea)
                    && Objects.equals(triggeredAreaEffects, other.triggeredAreaEffects);
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(", ", "Reactor [", "]");
            if (id != null) {
                sj.add("id=" + id.toString());
            }
            sj.add("armed=" + Boolean.toString(armed));
            if (disarmDifficulties != null && !disarmDifficulties.isEmpty()) {
                sj.add("disarmDifficulties=" + disarmDifficulties.toString());
            }
            if (armingDifficulties != null && !armingDifficulties.isEmpty()) {
                sj.add("amringDifficulties=" + armingDifficulties.toString());
            }
            if (onDisarmFailure != null && !onDisarmFailure.isEmpty()) {
                sj.add("onDisarmFailure=" + onDisarmFailure.toString());
            }
            if (onDisarmSuccess != null && !onDisarmSuccess.isEmpty()) {
                sj.add("onDisarmSuccess=" + onDisarmSuccess.toString());
            }
            if (onArmingFailure != null && !onArmingFailure.isEmpty()) {
                sj.add("onArmingFailure=" + onArmingFailure.toString());
            }
            if (triggeredAreaEffects != null && !triggeredAreaEffects.isEmpty()) {
                sj.add("triggeredAreaEffects=" + triggeredAreaEffects.toString());
            }
            if (eventfulArea != null) {
                sj.add("eventfulArea=" + eventfulArea.toString());
            }
            return sj.toString();
        }

    }

}
