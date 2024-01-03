package com.lhf.game.map;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.CreatureContainer;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.Player;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.PooledMessageChainHandler;
import com.lhf.server.client.user.UserID;
import com.lhf.server.interfaces.NotNull;

public abstract class SubArea implements CreatureContainer, PooledMessageChainHandler<ICreature>, Comparable<SubArea> {
    public static enum SubAreaSort {
        RECUPERATION, BATTLE;
    }

    protected final static int MAX_POOLED_ACTIONS = 1;
    protected final static int MAX_MILLISECONDS = 120000;
    protected final static int DEFAULT_MILLISECONDS = 90000;
    protected final SubAreaSort sort;
    protected final GameEventProcessorID gameEventProcessorID;
    protected final int roundDurationMilliseconds;
    protected final Logger logger;
    protected final transient Area area;
    protected transient EnumMap<CommandMessage, CommandHandler> cmds;
    protected NavigableMap<ICreature, Deque<IPoolEntry>> actionPools;

    public static abstract class SubAreaBuilder<SubAreaType extends SubArea, BuilderType extends SubAreaBuilder<SubAreaType, BuilderType>>
            implements Serializable {
        public static class SubAreaBuilderID implements Comparable<SubAreaBuilderID> {
            private final UUID id = UUID.randomUUID();

            @Override
            public int compareTo(SubAreaBuilderID arg0) {
                return this.id.compareTo(arg0.id);
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
        }

        protected final transient BuilderType thisObject;
        protected final SubAreaBuilderID id;
        private int waitMilliseconds;
        private Set<CreatureFilterQuery> creatureQueries;
        private boolean queryOnBuild;

        protected SubAreaBuilder() {
            this.thisObject = getThis();
            this.id = new SubAreaBuilderID();
            this.waitMilliseconds = DEFAULT_MILLISECONDS;
            this.creatureQueries = new HashSet<>();
            this.queryOnBuild = true;
        }

        protected abstract BuilderType getThis();

        public abstract SubAreaSort getSubAreaSort();

        public BuilderType setWaitMilliseconds(int count) {
            this.waitMilliseconds = Integer.min(SubArea.MAX_MILLISECONDS, Integer.max(1, count));
            return this.getThis();
        }

        public int getWaitMilliseconds() {
            return this.waitMilliseconds;
        }

        public BuilderType addCreatureQuery(CreatureFilterQuery query) {
            if (query != null) {
                this.creatureQueries.add(query);
            }
            return this.getThis();
        }

        public BuilderType resetCreatureQueries() {
            this.creatureQueries.clear();
            return this.getThis();
        }

        public Set<CreatureFilterQuery> getCreatureQueries() {
            return creatureQueries;
        }

        public boolean isQueryOnBuild() {
            return queryOnBuild;
        }

        public void setQueryOnBuild(boolean queryOnBuild) {
            this.queryOnBuild = queryOnBuild;
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
            SubAreaBuilder<?, ?> other = (SubAreaBuilder<?, ?>) obj;
            return Objects.equals(id, other.id);
        }

        public abstract SubAreaType build(@NotNull Area area);

    }

    protected SubArea(SubAreaBuilder<? extends SubArea, ?> builder, @NotNull Area area) {
        this.gameEventProcessorID = new GameEventProcessorID();
        this.sort = builder.getSubAreaSort();
        if (this.sort == null) {
            throw new NullPointerException("Builder must provide a sort!");
        }
        this.area = area;
        this.logger = Logger.getLogger(this.getClass().getName() + "." + this.getName().replaceAll("\\W", "_"));
        this.roundDurationMilliseconds = builder.getWaitMilliseconds();
        this.cmds = this.buildCommands();
        this.actionPools = Collections.synchronizedNavigableMap(new TreeMap<>());
        if (builder.isQueryOnBuild()) {
            for (final CreatureFilterQuery query : builder.getCreatureQueries()) {
                for (ICreature creature : this.area.filterCreatures(query)) {
                    this.addCreature(creature);
                }
            }
        }
    }

    public final SubAreaSort getSubAreaSort() {
        return this.sort;
    }

    protected abstract EnumMap<CommandMessage, CommandHandler> buildCommands();

    public final Area getArea() {
        return this.area;
    }

    @Override
    public Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx) {
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

    @Override
    public final boolean addCreature(ICreature creature) {
        boolean basicAdd = this.basicAddCreature(creature);
        if (basicAdd) {
            creature.setSuccessor(this);
            creature.addSubArea(getSubAreaSort());
            final NavigableSet<SubArea> areasSubAreas = this.area.getSubAreas();
            if (areasSubAreas == null) {
                return basicAdd;
            }
            final NavigableSet<SubArea> inferiors = areasSubAreas.headSet(this, false);
            for (final SubArea inferior : inferiors) {
                if (inferior.getCreatures().contains(creature)) {
                    creature.setSuccessor(inferior);
                }
            }
        }
        return basicAdd;
    }

    @Override
    public boolean addPlayer(Player player) {
        return this.addCreature(player);
    }

    protected abstract boolean basicRemoveCreature(ICreature creature);

    @Override
    public final boolean removeCreature(ICreature creature) {
        boolean basicRemove = this.basicRemoveCreature(creature);
        if (basicRemove) {
            creature.setSuccessor(this.area);
            creature.removeSubArea(getSubAreaSort());
            final NavigableSet<SubArea> areasSubAreas = this.area.getSubAreas();
            if (areasSubAreas == null) {
                return basicRemove;
            }
            final SubArea superior = areasSubAreas.higher(this);
            if (superior != null && superior.getCreatures().contains(creature)) {
                creature.setSuccessor(superior);
            }
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
        return this.area;
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

}
