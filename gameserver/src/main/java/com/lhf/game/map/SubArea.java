package com.lhf.game.map;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.lhf.game.CreatureContainer;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.Player;
import com.lhf.game.map.commandHandlers.SubAreaCastHandler;
import com.lhf.game.map.commandHandlers.SubAreaExitHandler;
import com.lhf.game.map.commandHandlers.SubAreaSayHandler;
import com.lhf.game.map.commandHandlers.SubAreaSeeHandler;
import com.lhf.game.map.commandHandlers.SubAreaShoutHandler;
import com.lhf.game.map.commandHandlers.SubAreaSpellbookHandler;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.PooledMessageChainHandler;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.server.client.user.UserID;
import com.lhf.server.interfaces.NotNull;

public abstract class SubArea implements CreatureContainer, PooledMessageChainHandler<ICreature>, Comparable<SubArea> {
    public static enum SubAreaSort {
        RECUPERATION, BATTLE;

        public boolean canBeAdded(final EnumSet<SubAreaSort> set) {
            if (set == null || set.isEmpty()) {
                return true;
            }
            for (final SubAreaSort sort : set) {
                if (this.compareTo(sort) > 0) {
                    return false;
                }
            }
            return true;
        }
    }

    public enum SubAreaCasting {
        NO_CASTING, POOLED_CASTING, FLUSH_CASTING;
    }

    protected final static int MAX_POOLED_ACTIONS = 1;
    public final static int MAX_MILLISECONDS = 120000;
    public final static int DEFAULT_MILLISECONDS = 90000;
    protected final SubAreaSort sort;
    protected final GameEventProcessorID gameEventProcessorID;
    protected final int roundDurationMilliseconds;
    protected final transient Logger logger;
    protected final transient Area area;
    protected final transient AtomicReference<RoundThread> roundThread;
    protected final SubAreaCasting allowCasting;
    protected transient EnumMap<AMessageType, CommandHandler> cmds;
    protected transient NavigableMap<ICreature, Deque<IPoolEntry>> actionPools;

    protected abstract class RoundThread extends Thread {
        protected final Logger logger;
        private final Phaser parentPhaser;
        private final Phaser roundPhaser;

        protected RoundThread() {
            this.logger = Logger.getLogger(SubArea.this.logger.getName() + "." + this.getClass().getSimpleName());
            this.logger.setLevel(SubArea.this.logger.getLevel());
            this.parentPhaser = new Phaser();
            this.roundPhaser = new Phaser(parentPhaser, SubArea.this.actionPools.size());
        }

        protected abstract void onThreadStart();

        protected abstract void onRoundStart();

        protected abstract void onRoundEnd();

        protected abstract void onThreadEnd();

        protected abstract void onRegister(final ICreature creature);

        @Override
        public void run() {
            try {
                this.parentPhaser.register();
                this.logger.log(Level.INFO, "Running");
                this.onThreadStart();
                while (!parentPhaser.isTerminated() && !roundPhaser.isTerminated()) {
                    this.logger.log(Level.INFO, () -> String.format("Phase start: %s", this));
                    this.onRoundStart();
                    this.logger.log(Level.FINE,
                            () -> String.format("Waiting for actions from: %s", SubArea.this.getCreatures().stream()
                                    .filter(c -> c != null && !SubArea.this.isReadyToFlush(c)).toList()));
                    try {
                        this.parentPhaser.awaitAdvanceInterruptibly(this.parentPhaser.arrive(),
                                SubArea.this.getTurnWaitCount(), TimeUnit.MILLISECONDS);
                        this.logger.log(Level.FINE, () -> String.format("Actions received -> Phase Update: %s", this));
                    } catch (TimeoutException e) {
                        synchronized (this.roundPhaser) {
                            this.logger.log(Level.INFO, () -> String.format("Timer ended round: %s, Thread: %s",
                                    SubArea.this.actionPools.toString(), this.toString()));
                            for (int i = this.roundPhaser.getUnarrivedParties(); i > 0; i--) {
                                this.roundPhaser.arrive();
                            }
                        }
                    } finally {
                        this.logger.log(Level.FINE, () -> String.format("Round ended: %s", this.toString()));
                        this.onRoundEnd();
                    }
                }
            } catch (InterruptedException e) {
                this.logger.log(Level.WARNING, e,
                        () -> String.format("Thread interrupted! %s %s", this.toString(), SubArea.this.toString()));
            } finally {
                this.logger.log(Level.INFO, () -> String.format("Ending Thread run: %s", this.toString()));
                this.onThreadEnd();
                synchronized (SubArea.this.roundThread) {
                    SubArea.this.roundThread.set(null);
                }
            }
        }

        public final void register(ICreature c) {
            if (c == null) {
                this.logger.log(Level.SEVERE,
                        String.format("Cannot register a null creature on this: %s", this));
                return;
            }
            synchronized (this.roundPhaser) {
                this.logger.log(Level.FINER,
                        () -> String.format("Attempting Registration of %s -> Phase pre-update: %s", c, this));
                this.roundPhaser.register();
                this.onRegister(c);
            }
        }

        protected abstract void onArriaval(final ICreature creature);

        public final void arrive(ICreature c) {
            if (c == null) {
                this.logger.log(Level.SEVERE,
                        String.format("Cannot arrive for a null creature on this: %s", this));
                return;
            }
            synchronized (this.roundPhaser) {
                this.logger.log(Level.FINER,
                        () -> String.format("Attempting Arrival %s -> Phase pre-update: %s",
                                c.getName(),
                                this));
                this.roundPhaser.arrive();
                this.onArriaval(c);
            }
        }

        protected abstract void onArriveAndDeregister(final ICreature creature);

        public final void arriveAndDeregister(ICreature c) {
            if (c == null) {
                this.logger.log(Level.SEVERE,
                        String.format("Cannot deregister for null creature on this: %s", this));
                return;
            }
            synchronized (this.roundPhaser) {
                this.logger.log(Level.FINER,
                        () -> String.format("Attempting Deregister %s -> Phase pre-update: %s",
                                c.getName(),
                                this));
                this.roundPhaser.arriveAndDeregister();
                this.onArriveAndDeregister(c);
            }
        }

        public final int getPhase() {
            synchronized (this.parentPhaser) {
                return this.parentPhaser.getPhase();
            }
        }

        public final RoundThread killIt() {
            synchronized (this.parentPhaser) {
                this.parentPhaser.forceTermination();
            }
            return this;
        }

        public boolean getIsRunning() {
            synchronized (this.parentPhaser) {
                return !this.parentPhaser.isTerminated() && this.parentPhaser.getRegisteredParties() > 0
                        && this.isAlive();
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(this.getClass().getSimpleName()).append(" [parentPhaser=").append(parentPhaser)
                    .append(", parentTerminated=").append(parentPhaser != null ? parentPhaser.isTerminated() : true)
                    .append(", roundPhaser=").append(roundPhaser).append("]");
            return builder.toString();
        }
    }

    public static interface ISubAreaBuildInfo extends Serializable {
        public final static class SubAreaBuilderID implements Comparable<SubAreaBuilderID> {
            private final UUID id;

            public SubAreaBuilderID() {
                this.id = UUID.randomUUID();
            }

            protected SubAreaBuilderID(@NotNull UUID id) {
                this.id = id;
            }

            @Override
            public int compareTo(SubAreaBuilderID arg0) {
                return this.id.compareTo(arg0.id);
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
                if (!(obj instanceof SubAreaBuilderID))
                    return false;
                SubAreaBuilderID other = (SubAreaBuilderID) obj;
                return Objects.equals(id, other.id);
            }

            @Override
            public String toString() {
                return this.id.toString();
            }

            public static class IDTypeAdapter extends TypeAdapter<SubAreaBuilderID> {

                @Override
                public void write(JsonWriter out, SubAreaBuilderID value) throws IOException {
                    out.value(value.getId().toString());
                }

                @Override
                public SubAreaBuilderID read(JsonReader in) throws IOException {
                    final String asStr = in.nextString();
                    return new SubAreaBuilderID(UUID.fromString(asStr));
                }

            }

        }

        public abstract SubAreaBuilderID getSubAreaBuilderID();

        public abstract SubAreaSort getSubAreaSort();

        public abstract SubAreaCasting isAllowCasting();

        public abstract int getWaitMilliseconds();

        public abstract Set<CreatureFilterQuery> getCreatureQueries();

        public abstract boolean isQueryOnBuild();

        public abstract void acceptBuildInfoVisitor(ISubAreaBuildInfoVisitor visitor);

        public abstract Level getLoggingLevel();
    }

    public static final class SubAreaBuilder implements ISubAreaBuildInfo {
        protected final SubAreaBuilderID id;
        protected final SubAreaSort sort;
        protected final String className;
        protected SubAreaCasting allowCasting;
        private int waitMilliseconds;
        private Set<CreatureFilterQuery> creatureQueries;
        private boolean queryOnBuild;
        private String loggingLevel;

        public SubAreaBuilder(SubAreaSort sort) {
            this.id = new SubAreaBuilderID();
            this.sort = sort;
            this.className = this.getClass().getName();
            this.waitMilliseconds = DEFAULT_MILLISECONDS;
            this.creatureQueries = new HashSet<>();
            this.queryOnBuild = true;
            this.allowCasting = SubAreaCasting.NO_CASTING;
            this.loggingLevel = null;
        }

        @Override
        public SubAreaBuilderID getSubAreaBuilderID() {
            return this.id;
        }

        public SubAreaSort getSubAreaSort() {
            return this.sort;
        }

        public SubAreaBuilder setAllowCasting(SubAreaCasting allowCasting) {
            this.allowCasting = allowCasting != null ? allowCasting : SubAreaCasting.NO_CASTING;
            return this;
        }

        public SubAreaCasting isAllowCasting() {
            return allowCasting != null ? allowCasting : SubAreaCasting.NO_CASTING;
        }

        public SubAreaBuilder setWaitMilliseconds(int count) {
            this.waitMilliseconds = Integer.min(SubArea.MAX_MILLISECONDS, Integer.max(1, count));
            return this;
        }

        public int getWaitMilliseconds() {
            return this.waitMilliseconds;
        }

        public SubAreaBuilder addCreatureQuery(CreatureFilterQuery query) {
            if (query != null) {
                this.creatureQueries.add(query);
            }
            return this;
        }

        public SubAreaBuilder resetCreatureQueries() {
            this.creatureQueries.clear();
            return this;
        }

        public Set<CreatureFilterQuery> getCreatureQueries() {
            return creatureQueries;
        }

        public boolean isQueryOnBuild() {
            return queryOnBuild;
        }

        public SubAreaBuilder setQueryOnBuild(boolean queryOnBuild) {
            this.queryOnBuild = queryOnBuild;
            return this;
        }

        @Override
        public void acceptBuildInfoVisitor(ISubAreaBuildInfoVisitor visitor) {
            visitor.visit(this);
        }

        public Level getLoggingLevel() {
            return this.loggingLevel != null ? Level.parse(this.loggingLevel) : null;
        }

        public SubAreaBuilder setLoggingLevel(Level loggingLevel) {
            this.loggingLevel = loggingLevel != null ? loggingLevel.getName() : null;
            return this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof SubAreaBuilder))
                return false;
            SubAreaBuilder other = (SubAreaBuilder) obj;
            return Objects.equals(id, other.id);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("SubAreaBuilder [id=").append(id).append(", sort=").append(sort).append(", className=")
                    .append(className).append(", allowCasting=").append(allowCasting).append(", waitMilliseconds=")
                    .append(waitMilliseconds).append(", creatureQueries=").append(creatureQueries)
                    .append(", queryOnBuild=").append(queryOnBuild).append("]");
            return builder.toString();
        }

    }

    protected SubArea(ISubAreaBuildInfo builder, @NotNull Area area) {
        this.gameEventProcessorID = new GameEventProcessorID();
        this.sort = builder.getSubAreaSort();
        if (this.sort == null) {
            throw new NullPointerException("Builder must provide a sort!");
        }
        this.area = area;
        this.logger = Logger.getLogger(this.getClass().getName() + "." + this.getName().replaceAll("\\W", "_"));
        this.logger.setLevel(builder.getLoggingLevel());
        this.roundDurationMilliseconds = builder.getWaitMilliseconds();
        this.allowCasting = builder.isAllowCasting();
        this.cmds = this.buildCommands();
        this.cmds.computeIfAbsent(AMessageType.EXIT, key -> new SubAreaExitHandler());
        this.cmds.computeIfAbsent(AMessageType.SPELLBOOK, key -> new SubAreaSpellbookHandler());
        switch (this.allowCasting) {
            case FLUSH_CASTING:
                this.cmds.computeIfAbsent(AMessageType.CAST, key -> new SubAreaCastHandler(true));
                break;
            case POOLED_CASTING:
                this.cmds.computeIfAbsent(AMessageType.CAST, key -> new SubAreaCastHandler(false));
                break;
            case NO_CASTING:
                break;
            default:
                break;

        }
        this.actionPools = Collections.synchronizedNavigableMap(new TreeMap<>());
        this.roundThread = new AtomicReference<>(null);
        if (builder.isQueryOnBuild()) {
            for (final CreatureFilterQuery query : builder.getCreatureQueries()) {
                for (ICreature creature : this.area.filterCreatures(query)) {
                    this.addCreature(creature);
                }
            }
        }
    }

    public abstract RoundThread instigate(ICreature instigator, Collection<ICreature> others);

    protected final int getTurnWaitCount() {
        return this.roundDurationMilliseconds;
    }

    public SubAreaCasting isAllowCasting() {
        return allowCasting;
    }

    public final SubAreaSort getSubAreaSort() {
        return this.sort;
    }

    public final RoundThread getRoundThread() {
        synchronized (this.roundThread) {
            return this.roundThread.get();
        }
    }

    public final boolean hasRunningThread(final String whoIsAsking) {
        synchronized (this.roundThread) {
            RoundThread thread = this.roundThread.get();
            if (thread == null) {
                this.log(Level.FINE, String.format("%s found null thread, not ongoing", whoIsAsking));
                return false;
            }
            return thread.getIsRunning();
        }
    }

    protected abstract EnumMap<AMessageType, CommandHandler> buildCommands();

    public final Area getArea() {
        return this.area;
    }

    @Override
    public Map<AMessageType, CommandHandler> getCommands(CommandContext ctx) {
        if (this.cmds == null) {
            this.cmds = this.buildCommands();
        }
        return Collections.unmodifiableMap(this.cmds);
    }

    @Override
    public final String getName() {
        return this.area.getName() + " " + this.getClass().getSimpleName();
    }

    @Override
    public final SeeEvent produceMessage() {
        return this.produceMessage(SeeEvent.getBuilder().setExaminable(this));
    }

    @Override
    public final GameEventProcessorID getEventProcessorID() {
        return this.gameEventProcessorID;
    }

    @Override
    public final String getColorTaggedName() {
        return this.getStartTag() + this.getName() + this.getEndTag();
    }

    @Override
    public NavigableMap<ICreature, Deque<IPoolEntry>> getPools() {
        return Collections.unmodifiableNavigableMap(this.actionPools);
    }

    public void clearPools(boolean flush) {
        if (flush) {
            this.flush();
        }
        this.actionPools.clear();
    }

    @Override
    public final Collection<ICreature> getCreatures() {
        return this.getPools().keySet();
    }

    public abstract void onAreaEntry(ICreature creature);

    protected abstract boolean basicAddCreature(ICreature creature);

    public static void additionSuccessorSet(SubArea subArea, ICreature creature) {
        if (subArea == null || creature == null) {
            return;
        }
        final NavigableSet<SubArea> areasSubAreas = subArea.getArea().getSubAreas();
        if (areasSubAreas == null) {
            return;
        }
        final NavigableSet<SubArea> inferiors = areasSubAreas.headSet(subArea, false);
        for (final SubArea inferior : inferiors) {
            if (inferior.getCreatures().contains(creature)) {
                creature.setSuccessor(inferior);
            }
        }
    }

    @Override
    public final boolean addCreature(ICreature creature) {
        boolean basicAdd = this.basicAddCreature(creature);
        if (basicAdd) {
            creature.setSuccessor(this);
            creature.addSubArea(getSubAreaSort());
            SubArea.additionSuccessorSet(this, creature);
            ICreature.eventAccepter.accept(creature, this.produceMessage());
        }
        return basicAdd;
    }

    @Override
    public boolean addPlayer(Player player) {
        return this.addCreature(player);
    }

    protected abstract boolean basicRemoveCreature(ICreature creature);

    public static void removalSuccessorSet(SubArea subArea, ICreature creature) {
        if (subArea == null || creature == null) {
            return;
        }
        final NavigableSet<SubArea> areasSubAreas = subArea.getArea().getSubAreas();
        if (areasSubAreas == null) {
            return;
        }
        final SubArea superior = areasSubAreas.higher(subArea);
        if (superior != null && superior.getCreatures().contains(creature)) {
            creature.setSuccessor(superior);
        } else {
            creature.setSuccessor(subArea.getArea());
        }
    }

    @Override
    public final boolean removeCreature(ICreature creature) {
        boolean basicRemove = this.basicRemoveCreature(creature);
        if (basicRemove) {
            creature.setSuccessor(this.area);
            creature.removeSubArea(getSubAreaSort());
            SubArea.removalSuccessorSet(this, creature);
            ICreature.eventAccepter.accept(creature, this.area.produceMessage());
        }
        return basicRemove;
    }

    @Override
    public Optional<ICreature> removeCreature(String name) {
        Optional<ICreature> found = this.getCreature(name);
        if (found.isPresent()) {
            this.removeCreature(found.get());
        }
        return found;
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
        Optional<Player> found = this.getPlayer(id);
        if (found.isPresent()) {
            this.removeCreature(found.get());
        }
        return found;
    }

    @Override
    public boolean removePlayer(Player player) {
        return this.removeCreature(player);
    }

    @Override
    public void setSuccessor(CommandChainHandler successor) {
        throw new UnsupportedOperationException("Cannot set the successor on a SubArea, it is bound to an Area!");
    }

    @Override
    public CommandChainHandler getSuccessor() {
        this.log(Level.FINEST, "Subareas limit available commands, and thus do not have successors");
        return null;
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        ctx.addSubArea(this);
        return ctx;
    }

    @Override
    public final synchronized void log(Level logLevel, String logMessage) {
        this.logger.log(logLevel, logMessage);
    }

    @Override
    public final synchronized void log(Level logLevel, Supplier<String> logMessageSupplier) {
        this.logger.log(logLevel, logMessageSupplier);
    }

    @Override
    public Collection<GameEventProcessor> getGameEventProcessors() {
        Collection<GameEventProcessor> messengers = CreatureContainer.super.getGameEventProcessors();
        return messengers;
    }

    @Override
    public final int compareTo(SubArea arg0) {
        if (this.area != null && arg0.area != null) {
            int areaCompare = this.area.compareTo(arg0.area);
            if (areaCompare != 0) {
                return areaCompare;
            }
        }
        return this.getSubAreaSort().compareTo(arg0.getSubAreaSort());
    }

    @Override
    public int hashCode() {
        return Objects.hash(area, this.getSubAreaSort());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof SubArea))
            return false;
        SubArea other = (SubArea) obj;
        return Objects.equals(area, other.area) && Objects.equals(this.getSubAreaSort(), other.getSubAreaSort());
    }

    public interface SubAreaCommandHandler extends CommandHandler {

        final static EnumMap<AMessageType, CommandHandler> subAreaCommandHandlers = new EnumMap<>(
                Map.of(AMessageType.EXIT, new SubAreaExitHandler(),
                        AMessageType.SAY, new SubAreaSayHandler(),
                        AMessageType.SEE, new SubAreaSeeHandler(),
                        AMessageType.SHOUT, new SubAreaShoutHandler()));

        final static EnumMap<AMessageType, CommandHandler> subAreaThirdPowerHandlers = new EnumMap<>(Map.of(
                AMessageType.CAST, new SubAreaCastHandler(true),
                AMessageType.SPELLBOOK, new SubAreaSpellbookHandler()));

        @Override
        public default boolean isEnabled(CommandContext ctx) {
            if (ctx == null) {
                return false;
            }
            ICreature creature = ctx.getCreature();
            if (creature == null || !creature.isAlive()) {
                return false;
            }
            final EnumSet<SubAreaSort> cSubs = ctx.getCreature().getSubAreaSorts();
            if (cSubs == null) {
                return false;
            }
            for (final SubAreaSort sort : cSubs) {
                if (!ctx.hasSubAreaSort(sort)) {
                    return false;
                }
            }
            return true;
        }

        default SubArea firstSubArea(CommandContext ctx) {
            for (final SubArea subArea : ctx.getSubAreas()) {
                if (subArea != null) {
                    return subArea;
                }
            }
            return null;
        }

        @Override
        default CommandChainHandler getChainHandler(CommandContext ctx) {
            return this.firstSubArea(ctx);
        }

    }

    public interface PooledSubAreaCommandHandler extends PooledCommandHandler {
        final static EnumMap<AMessageType, CommandHandler> subAreaThirdPowerHandlers = new EnumMap<>(Map.of(
                AMessageType.CAST, new SubAreaCastHandler(false),
                AMessageType.SPELLBOOK, new SubAreaSpellbookHandler()));

        public default SubArea firstSubArea(CommandContext ctx) {
            for (final SubArea subArea : ctx.getSubAreas()) {
                if (subArea != null) {
                    return subArea;
                }
            }
            return null;
        }

        @Override
        public default PooledMessageChainHandler<?> getPooledChainHandler(CommandContext ctx) {
            return this.firstSubArea(ctx);
        }

        @Override
        default CommandChainHandler getChainHandler(CommandContext ctx) {
            return this.getPooledChainHandler(ctx);
        }

        @Override
        public default boolean isEnabled(CommandContext ctx) {
            if (ctx == null) {
                return false;
            }
            ICreature creature = ctx.getCreature();
            if (creature == null || !creature.isAlive()) {
                return false;
            }
            final EnumSet<SubAreaSort> cSubs = ctx.getCreature().getSubAreaSorts();
            if (cSubs == null) {
                return false;
            }
            for (final SubAreaSort sort : cSubs) {
                if (!ctx.hasSubAreaSort(sort)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        default boolean isPoolingEnabled(CommandContext ctx) {
            final SubArea first = this.firstSubArea(ctx);
            if (first == null) {
                return false;
            }
            return this.isEnabled(ctx) && first.hasRunningThread(this.getClass().getName() + "::isPoolingEnabled(ctx)");
        }

    }
}
