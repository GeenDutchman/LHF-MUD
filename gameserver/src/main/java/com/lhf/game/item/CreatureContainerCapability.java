package com.lhf.game.item;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.RichOutput;
import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.game.CreatureContainer;
import com.lhf.game.IExternalReference;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureVisitor;
import com.lhf.game.creature.DungeonMaster;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.Monster;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.SummonedMonster;
import com.lhf.game.creature.SummonedNPC;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.map.Area;
import com.lhf.game.map.Area.AreaReference;
import com.lhf.game.map.Directions;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.BadGoEvent;
import com.lhf.messages.events.BadGoEvent.BadGoType;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.ItemInteractionEvent;
import com.lhf.messages.events.SeeEvent.ABuilder;
import com.lhf.messages.events.SeeEvent.SeeCategory;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.ExitMessage;
import com.lhf.messages.in.GoMessage;
import com.lhf.messages.in.InteractMessage;
import com.lhf.messages.in.SayMessage;
import com.lhf.messages.in.ShoutMessage;
import com.lhf.server.client.user.UserID;

public interface CreatureContainerCapability extends ItemCapability, CreatureContainer, CommandChainHandler {

    @Override
    default ItemCapabilityNames getCapabilityName() {
        return ItemCapabilityNames.PEN;
    }

    @Override
    default void describe(ABuilder<?> seeEventBuilder) {
        if (seeEventBuilder == null) {
            return;
        }
        this.getCreatures().stream().forEach(new CreatureVisitor() {

            @Override
            public void visit(Player player) {
                if (player != null) {
                    seeEventBuilder.addSeen(SeeCategory.PLAYER, player);
                }
            }

            @Override
            public void visit(NonPlayerCharacter npc) {
                if (npc != null) {
                    seeEventBuilder.addSeen(SeeCategory.NPC, npc);
                }
            }

            @Override
            public void visit(DungeonMaster dungeonMaster) {
                if (dungeonMaster != null) {
                    seeEventBuilder.addSeen(SeeCategory.CREATURE, dungeonMaster);
                }
            }

            @Override
            public void visit(SummonedNPC sNpc) {
                if (sNpc != null) {
                    seeEventBuilder.addSeen(SeeCategory.NPC, sNpc);
                }
            }

            @Override
            public void visit(Monster monster) {
                if (monster != null) {
                    seeEventBuilder.addSeen(SeeCategory.MONSTER, monster);
                }
            }

            @Override
            public void visit(SummonedMonster sMonster) {
                if (sMonster != null) {
                    seeEventBuilder.addSeen(SeeCategory.MONSTER, sMonster);
                }
            }

        });
    }

    @Override
    default String getDescription() {
        return "This item can contain creatures.";
    }

    @Override
    public default String getName() {
        return "Pen";
    }

    @Override
    public default String getTagName() {
        return "CreaturePen";
    }

    public List<IExternalReference<Area>> getExternalAreaReferences();

    public default Area getContainerArea() {
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

    public Set<CreatureEffectSource> getCreatureTermEffects();

    public RichOutput getTermOutput();

    public boolean isInRoom(ICreature creature);

    public Duration getTermDuration();

    public default Integer getCapacity() {
        return -1;
    }

    public default boolean hasCapacity() {
        final Integer cap = this.getCapacity();
        final Collection<ICreature> creatures = this.getCreatures();
        if (cap == null || cap <= 0 || (creatures != null && creatures.size() < cap)) {
            return true;
        }
        return false;
    }

    public default Set<Attributes> getOrderingCheckAttributes() {
        return Collections.unmodifiableSet(EnumSet.of(Attributes.DEX));
    }

    @Override
    default boolean isStateful() {
        return false;
    }

    public default Logger getLogger() {
        return Logger
                .getLogger(String.format("%s.%s", this.getClass().getName(), this.getEventProcessorID().toString()));
    }

    public CreatureContainerCapability setLogger(Logger logger);

    public enum OccupantOrdering {
        SORTED, CHECKED, HASHED;
    }

    public static CreatureContainerCapability generateCreatureContainerCapability() {
        return new CreaturePen(new CreaturePen.Builder());
    }

    public static final class CreaturePen implements CreatureContainerCapability {
        protected final class PennedCreature implements Runnable, Comparable<PennedCreature> {
            protected final transient ICreature creature;
            protected final Integer ordering;
            protected final transient CommandChainHandler successor;
            protected transient ScheduledFuture<?> future;

            protected PennedCreature(ICreature creature) {
                this.creature = creature;
                this.ordering = null;
                this.successor = creature.getSuccessor();
                this.creature.setSuccessor(CreaturePen.this);
            }

            protected PennedCreature(ICreature creature, int ordering) {
                this.creature = creature;
                this.ordering = ordering;
                this.successor = creature.getSuccessor();
                this.creature.setSuccessor(CreaturePen.this);
            }

            public PennedCreature setFuture(ScheduledFuture<?> future) {
                this.future = future;
                return this;
            }

            public boolean cancel() {
                if (this.future == null) {
                    return false;
                }
                return this.future.cancel(true);
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + getEnclosingInstance().hashCode();
                result = prime * result + Objects.hash(creature, ordering, successor);
                return result;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (!(obj instanceof PennedCreature))
                    return false;
                PennedCreature other = (PennedCreature) obj;
                if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
                    return false;
                return Objects.equals(creature, other.creature) && Objects.equals(ordering, other.ordering)
                        && Objects.equals(successor, other.successor);
            }

            private CreaturePen getEnclosingInstance() {
                return CreaturePen.this;
            }

            @Override
            public int compareTo(PennedCreature arg0) {
                int comparison = this.creature.compareTo(arg0.creature);
                if (this.ordering != null && arg0.ordering == null) {
                    return -1;
                } else if (this.ordering == null && arg0.ordering != null) {
                    return 1;
                } else if (this.ordering == null && arg0.ordering == null) {
                    return comparison;
                }
                int orderComparison = Integer.compare(this.ordering, arg0.ordering);
                if (orderComparison != 0) {
                    return orderComparison;
                }
                return comparison;
            }

            @Override
            public void run() {
                if (!this.getEnclosingInstance().isInRoom(creature)) {
                    this.future.cancel(false);
                    this.getEnclosingInstance().occupants.remove(this);
                }
                ItemInteractionEvent.Builder iom = ItemInteractionEvent.getBuilder().setPerformed()
                        .setInteractor(creature).setOutputCallback(rob -> {
                            if (rob == null) {
                                return;
                            }
                            rob.appendRichOutput(this.getEnclosingInstance().getTermOutput());
                        });
                ICreature.eventAccepter.accept(creature, iom.Build());
                final Set<CreatureEffectSource> termEffects = this.getEnclosingInstance().getCreatureTermEffects();
                if (termEffects != null) {
                    for (CreatureEffectSource source : this.getEnclosingInstance().getCreatureTermEffects()) {
                        if (source == null) {
                            continue;
                        }
                        GameEvent done = creature
                                .applyEffect(new CreatureEffect(source, creature, this.getEnclosingInstance()));
                        ICreature.eventAccepter.accept(creature, done);
                    }
                }

            }

        }

        private OccupantOrdering ordering;
        private final GameEventProcessorID gameEventProcessorID = new GameEventProcessorID();
        protected transient Set<PennedCreature> occupants;
        private transient ScheduledThreadPoolExecutor executor;
        private final Duration termDuration;
        private final AreaReference containedArea;
        private final Set<CreatureEffectSource> creatureTermEffects;
        private final RichOutput termOutput;
        private final EnumSet<Attributes> orderingCheckAttributes;
        private final Integer capacity;
        private transient Logger logger = Logger
                .getLogger(String.format("%s.%s", this.getClass().getName(), this.gameEventProcessorID.toString()));

        public static class Builder {
            private OccupantOrdering ordering = OccupantOrdering.HASHED;
            private Duration termDuration = Duration.ofSeconds(2);
            private AreaReference containedArea;
            private Set<CreatureEffectSource> creatureTermEffects;
            private RichOutputBuilder termOutput;
            private EnumSet<Attributes> orderingCheckAttributes = EnumSet.of(Attributes.DEX);
            private Integer capacity = 1;

            public OccupantOrdering getOrdering() {
                return ordering;
            }

            public Builder setOrdering(OccupantOrdering ordering) {
                this.ordering = ordering;
                return this;
            }

            public Duration getTermDuration() {
                return termDuration;
            }

            public Builder setTermDuration(Duration termDuration) {
                this.termDuration = termDuration;
                return this;
            }

            public AreaReference getContainedArea() {
                return containedArea;
            }

            public Builder setContainedArea(AreaReference area) {
                if (area != null) {
                    this.containedArea = area;
                }
                return this;
            }

            public Set<CreatureEffectSource> getCreatureTermEffects() {
                return creatureTermEffects;
            }

            public Builder setCreatureTermEffects(Set<CreatureEffectSource> creatureTermEffects) {
                this.creatureTermEffects = creatureTermEffects;
                return this;
            }

            public Builder adjustCreatureTermEffects(Consumer<Set<CreatureEffectSource>> adjuster) {
                if (adjuster != null) {
                    if (this.creatureTermEffects == null) {
                        this.creatureTermEffects = new LinkedHashSet<>();
                    }
                    adjuster.accept(this.creatureTermEffects);
                }
                return this;
            }

            public Builder addCreatureTermEffect(CreatureEffectSource source) {
                if (source != null) {
                    if (this.creatureTermEffects == null) {
                        this.creatureTermEffects = new LinkedHashSet<>();
                    }
                    this.creatureTermEffects.add(source);
                }
                return this;
            }

            public Builder clearCreatureTermEffects() {
                if (this.creatureTermEffects != null) {
                    this.creatureTermEffects.clear();
                }
                return this;
            }

            public RichOutputBuilder getTermOutput() {
                return termOutput;
            }

            public RichOutput buildTermOutput() {
                return this.termOutput != null ? this.termOutput.build() : null;
            }

            public Builder setTermOutput(RichOutputBuilder termOutput) {
                this.termOutput = termOutput;
                return this;
            }

            public Builder adjustTermOutput(Consumer<RichOutputBuilder> adjustor) {
                if (adjustor != null) {
                    if (this.termOutput == null) {
                        this.termOutput = new RichOutputBuilder();
                    }
                    adjustor.accept(termOutput);
                }
                return this;
            }

            public EnumSet<Attributes> getOrderingCheckAttributes() {
                return orderingCheckAttributes;
            }

            public Builder setOrderingCheckAttributes(EnumSet<Attributes> orderingCheckAttributes) {
                this.orderingCheckAttributes = orderingCheckAttributes;
                return this;
            }

            public Builder addOrderingCheckAttribute(Attributes checkAttribute) {
                if (checkAttribute != null) {
                    if (this.orderingCheckAttributes == null) {
                        this.orderingCheckAttributes = EnumSet.noneOf(Attributes.class);
                    }
                    this.orderingCheckAttributes.add(checkAttribute);
                }
                return this;
            }

            public Builder adjustOrderingCheckAttribute(Consumer<Set<Attributes>> adjustor) {
                if (adjustor != null) {
                    if (this.orderingCheckAttributes == null) {
                        this.orderingCheckAttributes = EnumSet.noneOf(Attributes.class);
                    }
                    adjustor.accept(this.orderingCheckAttributes);
                }
                return this;
            }

            public Builder clearOrderingCheckAttributes() {
                if (this.orderingCheckAttributes != null) {
                    this.orderingCheckAttributes.clear();
                }
                return this;
            }

            public Integer getCapacity() {
                return capacity;
            }

            public Builder setCapacity(Integer capacity) {
                this.capacity = capacity;
                return this;
            }

            public CreaturePen build() {
                return new CreaturePen(this);
            }

        }

        public static Builder getBuilder() {
            return new Builder();
        }

        private CreaturePen(Builder builder) {
            if (builder == null) {
                this.ordering = OccupantOrdering.HASHED;
                this.termDuration = Duration.ofSeconds(3);
                this.containedArea = null;
                this.creatureTermEffects = Set.of();
                this.termOutput = null;
                this.orderingCheckAttributes = EnumSet
                        .copyOf(CreatureContainerCapability.super.getOrderingCheckAttributes());
                this.capacity = 1;
            } else {
                this.ordering = builder.getOrdering();
                this.termDuration = builder.getTermDuration();
                this.containedArea = builder.getContainedArea();
                this.creatureTermEffects = builder.creatureTermEffects != null ? Set.copyOf(builder.creatureTermEffects)
                        : Set.of();
                this.termOutput = builder.buildTermOutput();
                this.orderingCheckAttributes = builder.orderingCheckAttributes != null
                        ? EnumSet.copyOf(builder.orderingCheckAttributes)
                        : EnumSet.noneOf(Attributes.class);
                this.capacity = builder.getCapacity();
            }
        }

        /**
         * Get the "singleton" executor
         * 
         * @return
         */
        private synchronized ScheduledThreadPoolExecutor getExecutor() {
            if (this.executor == null) {
                this.executor = new ScheduledThreadPoolExecutor(Integer.max(this.getCapacity(), 1));
                this.executor.setRemoveOnCancelPolicy(true);
                this.executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
                this.executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
            }
            return this.executor;
        }

        public GameEventProcessorID getGameEventProcessorID() {
            return gameEventProcessorID;
        }

        @Override
        public Set<Attributes> getOrderingCheckAttributes() {
            return this.orderingCheckAttributes != null ? Collections.unmodifiableSet(this.orderingCheckAttributes)
                    : Set.of();
        }

        @Override
        public Integer getCapacity() {
            return this.capacity;
        }

        @Override
        public Duration getTermDuration() {
            return this.termDuration;
        }

        private synchronized Set<PennedCreature> getOccupants() {
            if (occupants == null) {
                if (this.ordering == null) {
                    this.occupants = new LinkedHashSet<>();
                } else {
                    switch (this.ordering) {
                    case CHECKED:
                    case SORTED:
                        this.occupants = new TreeSet<>();
                        break;
                    case HASHED:
                    default:
                        this.occupants = new LinkedHashSet<>();
                        break;

                    }
                }
            }
            return this.occupants;
        }

        @Override
        public synchronized Collection<ICreature> getCreatures() {
            Collection<ICreature> creatures;
            if (this.ordering == null) {
                creatures = new LinkedHashSet<>();
            } else {
                switch (this.ordering) {
                case CHECKED:
                    // fallthrough
                case SORTED:
                    creatures = new TreeSet<>();
                    break;
                case HASHED:
                    // fallthrough
                default:
                    creatures = new LinkedHashSet<>();
                    break;
                }
            }
            for (final Iterator<PennedCreature> iter = this.getOccupants().iterator(); iter.hasNext();) {
                final PennedCreature penned = iter.next();
                if (penned == null || penned.creature == null) {
                    iter.remove();
                    continue;
                }
                creatures.add(penned.creature);
            }
            return Collections.unmodifiableCollection(creatures);
        }

        @Override
        public boolean onCreatureDeath(ICreature creature) {
            boolean removed = this.removeCreature(creature);
            final Area area = this.getContainerArea();
            if (area != null) {
                removed = area.onCreatureDeath(creature) || removed;
            }
            return removed;
        }

        protected PennedCreature getPennedCreature(ICreature creature) {
            if (creature == null) {
                return null;
            }
            for (PennedCreature penned : this.getOccupants()) {
                if (penned.creature == creature) {
                    return penned;
                }
            }
            return null;
        }

        protected PennedCreature penCreature(ICreature creature) {
            if (creature == null) {
                return null;
            }

            PennedCreature penned = this.getPennedCreature(creature);
            if (penned != null) {
                return penned;
            }
            if (this.ordering == null) {
                penned = new PennedCreature(creature);
            } else {
                switch (this.ordering) {
                case CHECKED:
                    final Set<Attributes> attrs = this.getOrderingCheckAttributes();
                    Attributes best = creature.getHighestAttributeBonus(attrs);
                    MultiRollResult checkResult = creature.check(best);
                    penned = new PennedCreature(creature, checkResult != null ? checkResult.getRoll() : 0);
                    break;
                case SORTED:
                case HASHED:
                default:
                    penned = new PennedCreature(creature);
                }
            }
            penned.setFuture(
                    this.getExecutor().scheduleWithFixedDelay(penned, TimeUnit.MILLISECONDS.convert(this.termDuration),
                            TimeUnit.MILLISECONDS.convert(this.termDuration), TimeUnit.MILLISECONDS));
            return penned;
        }

        @Override
        public synchronized Collection<ICreature> filterCreatures(CreatureFilterQuery query) {
            if (query == null) {
                return Set.of();
            }
            final Set<ICreature> creatures;
            if (this.ordering == null) {
                creatures = new LinkedHashSet<>();
            } else {
                switch (this.ordering) {
                case CHECKED:
                    // fallthrough
                case SORTED:
                    creatures = new TreeSet<>();
                    break;
                case HASHED:
                    // fallthrough
                default:
                    creatures = new LinkedHashSet<>();
                    break;
                }
            }
            for (final Iterator<PennedCreature> iter = this.getOccupants().iterator(); iter.hasNext();) {
                final PennedCreature penned = iter.next();
                if (penned == null || penned.creature == null) {
                    iter.remove();
                    continue;
                }
                if (query.test(penned.creature)) {
                    creatures.add(penned.creature);
                }
            }
            return Collections.unmodifiableCollection(creatures);
        }

        @Override
        public synchronized boolean addCreature(ICreature creature) {
            if (creature == null) {
                return false;
            }
            if (!this.hasCapacity()) {
                return false;
            }
            PennedCreature penned = this.penCreature(creature);
            if (penned == null) {
                return false;
            }
            // log
            final Set<PennedCreature> retrieved = this.getOccupants();
            return retrieved.add(penned);
        }

        @Override
        public synchronized Optional<ICreature> removeCreature(String name) {
            for (final Iterator<PennedCreature> iter = this.getOccupants().iterator(); iter.hasNext();) {
                final PennedCreature penned = iter.next();
                if (penned == null || penned.creature == null) {
                    iter.remove();
                    continue;
                }
                if (penned.creature.checkName(name)) {
                    iter.remove();
                    return Optional.ofNullable(penned.creature);
                }
            }
            return Optional.empty();
        }

        public synchronized Optional<PennedCreature> removeOccupant(ICreature creature) {
            for (final Iterator<PennedCreature> iter = this.getOccupants().iterator(); iter.hasNext();) {
                final PennedCreature penned = iter.next();
                if (penned == null || penned.creature == null) {
                    iter.remove();
                    continue;
                }
                if (penned.creature.equals(creature)) {
                    iter.remove();
                    return Optional.of(penned);
                }
            }
            return Optional.empty();
        }

        @Override
        public synchronized boolean removeCreature(ICreature creature) {
            for (final Iterator<PennedCreature> iter = this.getOccupants().iterator(); iter.hasNext();) {
                final PennedCreature penned = iter.next();
                if (penned == null || penned.creature == null) {
                    iter.remove();
                    continue;
                }
                if (penned.creature.equals(creature)) {
                    iter.remove();
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean addPlayer(Player player) {
            return this.addCreature(player);
        }

        @Override
        public Optional<Player> removePlayer(String name) {
            Optional<Player> found = this.getPlayer(name);
            if (found.isPresent()) {
                this.removeCreature(found.get());
            }
            return found;
        }

        @Override
        public Optional<Player> removePlayer(UserID id) {
            Optional<Player> toRemove = this.getPlayer(id);
            if (toRemove.isPresent()) {
                this.removeCreature(toRemove.get());
            }
            return toRemove;
        }

        @Override
        public boolean removePlayer(Player player) {
            return this.removeCreature(player);
        }

        @Override
        public Logger getLogger() {
            if (this.logger == null) {
                this.logger = CreatureContainerCapability.super.getLogger();
            }
            return this.logger;
        }

        @Override
        public CreaturePen setLogger(Logger nextLogger) {
            this.logger = nextLogger;
            return this;
        }

        @Override
        public void log(Level logLevel, String logMessage) {
            this.getLogger().log(logLevel, logMessage);
        }

        @Override
        public void log(Level logLevel, Supplier<String> logMessageSupplier) {
            this.getLogger().log(logLevel, logMessageSupplier);
        }

        @Override
        public GameEventProcessorID getEventProcessorID() {
            return this.gameEventProcessorID;
        }

        @Override
        public void setSuccessor(CommandChainHandler successor) {
            throw new UnsupportedOperationException("Unimplemented method 'setSuccessor'");
        }

        @Override
        public CommandChainHandler getSuccessor() {
            return null; // pretend there is none
        }

        @Override
        public CommandContext addSelfToContext(CommandContext ctx) {
            return ctx;
        }

        @Override
        public Map<AMessageType, CommandHandler> getCommands(CommandContext ctx) {
            return Map.of(AMessageType.GO, new ICreature.CreatureCommandHandler() {

                @Override
                public AMessageType getHandleType() {
                    return AMessageType.GO;
                }

                @Override
                public Optional<String> getHelp(CommandContext ctx) {
                    return Optional.of("Use the command \"GO UP\" to get out of this.");
                }

                @Override
                public CommandChainHandler getChainHandler(CommandContext ctx) {
                    return CreaturePen.this;
                }

                @Override
                public Reply visit(CommandContext ctx, GoMessage command) {
                    if (command == null) {
                        return ctx.failhandle();
                    }
                    if (Directions.UP.equals(command.getDirection())) {
                        CreaturePen.this.removeCreature(ctx.getCreature());
                        return ctx.handled();
                    } else {
                        ctx.receive(BadGoEvent.getBuilder().setSubType(BadGoType.DNE)
                                .setAttempted(command.getDirection()).setAvailable(EnumSet.of(Directions.UP)).Build());
                        return ctx.handled();
                    }
                }

            }, AMessageType.EXIT, new ICreature.CreatureCommandHandler() {

                @Override
                public AMessageType getHandleType() {
                    return AMessageType.EXIT;
                }

                @Override
                public Optional<String> getHelp(CommandContext ctx) {
                    return Optional.of("Disconnect and leave Ibaif!");
                }

                @Override
                public CommandChainHandler getChainHandler(CommandContext ctx) {
                    return CreaturePen.this;
                }

                @Override
                public Reply visit(CommandContext ctx, ExitMessage command) {
                    if (command == null) {
                        return ctx.failhandle();
                    }
                    final Optional<PennedCreature> wasPenned = CreaturePen.this.removeOccupant(ctx.getCreature());
                    final Area area = CreaturePen.this.getContainerArea();
                    if (area != null) {
                        return area.applyChain(ctx, command);
                    }
                    if (wasPenned.isPresent()) {
                        final PennedCreature pennedCreature = wasPenned.get();
                        return CommandChainHandler.passUpChain(pennedCreature.successor, ctx, command);
                    }
                    return CommandChainHandler.passUpChain(CreaturePen.this, ctx, command);
                }

            }, AMessageType.INTERACT, new ICreature.CreatureCommandHandler() {

                @Override
                public AMessageType getHandleType() {
                    return AMessageType.INTERACT;
                }

                @Override
                public Optional<String> getHelp(CommandContext ctx) {
                    return Optional
                            .of(String.format("Use the command <command>INTERACT %s</command> to get out of here.",
                                    CreaturePen.this.getName()));
                }

                @Override
                public CommandChainHandler getChainHandler(CommandContext ctx) {
                    return CreaturePen.this;
                }

                @Override
                public Reply visit(CommandContext ctx, InteractMessage command) {
                    if (command == null) {
                        return ctx.failhandle();
                    }
                    if (CreaturePen.this.getName().equalsIgnoreCase(command.getObject())) {
                        CreaturePen.this.removeCreature(ctx.getCreature());
                        return ctx.handled();
                    }
                    return ctx.failhandle();
                }

            }, AMessageType.SAY, new ICreature.CreatureCommandHandler() {

                @Override
                public AMessageType getHandleType() {
                    return AMessageType.SAY;
                }

                @Override
                public Optional<String> getHelp(CommandContext ctx) {
                    return Optional.of("Says stuff to people in the area.");
                }

                @Override
                public CommandChainHandler getChainHandler(CommandContext ctx) {
                    return CreaturePen.this;
                }

                @Override
                public Reply visit(CommandContext ctx, SayMessage command) {
                    if (command == null) {
                        return ctx.failhandle();
                    }
                    final Area area = CreaturePen.this.getContainerArea();
                    if (area != null) {
                        return area.applyChain(ctx, command);
                    }
                    final PennedCreature wasPenned = CreaturePen.this.getPennedCreature(ctx.getCreature());
                    if (wasPenned != null) {
                        return CommandChainHandler.passUpChain(wasPenned.successor, ctx, command);
                    }
                    return CommandChainHandler.passUpChain(CreaturePen.this, ctx, command);
                }

            }, AMessageType.SHOUT, new ICreature.CreatureCommandHandler() {

                @Override
                public AMessageType getHandleType() {
                    return AMessageType.SHOUT;
                }

                @Override
                public Optional<String> getHelp(CommandContext ctx) {
                    return Optional.of("Shouts stuff to people in the area.");
                }

                @Override
                public CommandChainHandler getChainHandler(CommandContext ctx) {
                    return CreaturePen.this;
                }

                @Override
                public Reply visit(CommandContext ctx, ShoutMessage command) {
                    if (command == null) {
                        return ctx.failhandle();
                    }
                    final Area area = CreaturePen.this.getContainerArea();
                    if (area != null) {
                        return area.applyChain(ctx, command);
                    }
                    final PennedCreature wasPenned = CreaturePen.this.getPennedCreature(ctx.getCreature());
                    if (wasPenned != null) {
                        return CommandChainHandler.passUpChain(wasPenned.successor, ctx, command);
                    }
                    return CommandChainHandler.passUpChain(CreaturePen.this, ctx, command);
                }
            });
        }

        @Override
        public Set<CreatureEffectSource> getCreatureTermEffects() {
            return this.creatureTermEffects != null ? Collections.unmodifiableSet(this.creatureTermEffects) : Set.of();
        }

        @Override
        public RichOutput getTermOutput() {
            return this.termOutput;
        }

        @Override
        public boolean isInRoom(ICreature creature) {
            if (creature == null) {
                return false;
            }
            final Area retrieved = this.getContainerArea();
            if (retrieved != null && retrieved.hasCreature(creature)) {
                return true;
            }
            return false;
        }

        @Override
        public List<IExternalReference<Area>> getExternalAreaReferences() {
            return List.of(this.containedArea);
        }

    }
}
