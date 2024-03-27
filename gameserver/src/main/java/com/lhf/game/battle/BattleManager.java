package com.lhf.game.battle;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.game.battle.BattleStats.BattleStatsQuery;
import com.lhf.game.battle.commandHandlers.BattleAttackHandler;
import com.lhf.game.battle.commandHandlers.BattleCastHandler;
import com.lhf.game.battle.commandHandlers.BattleGoHandler;
import com.lhf.game.battle.commandHandlers.BattlePassHandler;
import com.lhf.game.battle.commandHandlers.BattleUseHandler;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.Player;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.Stats;
import com.lhf.game.map.Area;
import com.lhf.game.map.ISubAreaBuildInfoVisitor;
import com.lhf.game.map.SubArea;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.PooledMessageChainHandler;
import com.lhf.messages.events.BattleJoinedEvent;
import com.lhf.messages.events.BattleOverEvent;
import com.lhf.messages.events.BattleRoundEvent;
import com.lhf.messages.events.BattleRoundEvent.RoundAcceptance;
import com.lhf.messages.events.BattleStartedEvent;
import com.lhf.messages.events.BattleStatsRequestedEvent;
import com.lhf.messages.events.FactionReinforcementsCallEvent;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.server.client.user.UserID;

public class BattleManager extends SubArea {
    BattleStats battleStats;

    public static interface IBattleManagerBuildInfo extends ISubAreaBuildInfo {

    }

    public static final class Builder implements IBattleManagerBuildInfo {
        protected final SubAreaBuilderID id;
        protected final String className;
        protected final SubAreaBuilder delegate;

        public Builder() {
            this.id = new SubAreaBuilderID();
            this.className = this.getClass().getName();
            this.delegate = new SubAreaBuilder(SubAreaSort.BATTLE).setAllowCasting(SubAreaCasting.POOLED_CASTING);
        }

        public static Builder getInstance() {
            return new Builder();
        }

        @Override
        public SubAreaBuilderID getSubAreaBuilderID() {
            return this.id;
        }

        @Override
        public SubAreaSort getSubAreaSort() {
            return delegate.getSubAreaSort();
        }

        public SubAreaCasting isAllowCasting() {
            return delegate.isAllowCasting();
        }

        public Builder setWaitMilliseconds(int count) {
            delegate.setWaitMilliseconds(count);
            return this;
        }

        public int getWaitMilliseconds() {
            return delegate.getWaitMilliseconds();
        }

        public Builder addCreatureQuery(CreatureFilterQuery query) {
            delegate.addCreatureQuery(query);
            return this;
        }

        public Builder resetCreatureQueries() {
            delegate.resetCreatureQueries();
            return this;
        }

        public Set<CreatureFilterQuery> getCreatureQueries() {
            return delegate.getCreatureQueries();
        }

        public boolean isQueryOnBuild() {
            return delegate.isQueryOnBuild();
        }

        public Builder setQueryOnBuild(boolean queryOnBuild) {
            delegate.setQueryOnBuild(queryOnBuild);
            return this;
        }

        @Override
        public void acceptBuildInfoVisitor(ISubAreaBuildInfoVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public Level getLoggingLevel() {
            return delegate.getLoggingLevel();
        }

        public Builder setLoggingLevel(Level loggingLevel) {
            delegate.setLoggingLevel(loggingLevel);
            return this;
        }

        public BattleManager build(Area area) {
            return new BattleManager(this, area);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, className, delegate);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Builder))
                return false;
            Builder other = (Builder) obj;
            return Objects.equals(id, other.id) && Objects.equals(className, other.className)
                    && Objects.equals(delegate, other.delegate);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Builder [id=").append(id).append(", className=").append(className).append(", delegate=")
                    .append(delegate).append("]");
            return builder.toString();
        }

    }

    protected class BattleRoundThread extends RoundThread {

        protected BattleRoundThread() {
            super();
        }

        @Override
        public void onThreadStart() {
            // does nothing so far
        }

        @Override
        public void onRoundStart() {
            Set<ICreature> actionsNeeded = new TreeSet<>();
            for (final ICreature creature : BattleManager.this.getCreatures()) {
                if (creature == null) {
                    continue;
                }
                if (!BattleManager.this.isReadyToFlush(creature)) {
                    actionsNeeded.add(creature);
                }
            }
            BattleManager.this.announceDirect(
                    BattleRoundEvent.getBuilder().setNeedSubmission(BattleRoundEvent.RoundAcceptance.NEEDED)
                            .setNotBroadcast().setRoundCount(this.getPhase()).Build(),
                    actionsNeeded);
        }

        @Override
        public void onRoundEnd() {
            this.logger.log(Level.FINE, "Ending Round");
            BattleManager.this.flush();
            BattleManager.this.battleStats.initialize(getCreatures());
            BattleManager.this.clearDead();
            if (!BattleManager.this.checkCompetingFactionsPresent("endRound()")) {
                this.killIt();
                return;
            }
            BattleManager.this.announce(BattleRoundEvent.getBuilder().setNeedSubmission(RoundAcceptance.COMPLETED));
            BattleManager.this.callReinforcements();
        }

        @Override
        public void onThreadEnd() {
            BattleManager.this.endBattle();
        }

        @Override
        protected void onRegister(ICreature creature) {
            // does nothing
        }

        @Override
        protected void onArriaval(ICreature creature) {
            // does nothing
        }

        @Override
        protected void onArriveAndDeregister(ICreature creature) {
            // does nothing
        }

    }

    public BattleManager(IBattleManagerBuildInfo builder, Area room) {
        super(builder, room);
        this.battleStats = new BattleStats().initialize(this.getCreatures());
    }

    @Override
    protected EnumMap<AMessageType, CommandHandler> buildCommands() {
        EnumMap<AMessageType, CommandHandler> cmds = new EnumMap<>(
                SubArea.SubAreaCommandHandler.subAreaCommandHandlers);
        cmds.putAll(SubArea.PooledSubAreaCommandHandler.subAreaThirdPowerHandlers);
        cmds.putAll(PooledBattleManagerCommandHandler.pooledBattleManagerCommandHandlers);
        cmds.put(AMessageType.STATS, new BattleStatsHandler());
        return cmds;
    }

    @Override
    public String getEndTag() {
        return "</battle>";
    }

    @Override
    public String getStartTag() {
        return "<battle>";
    }

    @Override
    public void onAreaEntry(ICreature creature) {
        if (creature == null) {
            return;
        }
        if (CreatureFaction.NPC.equals(creature.getFaction())) {
            return;
        }
        if (this.hasRunningThread(String.format("onAreaEntry(%s)", creature))) {
            this.addCreature(creature);
        }
    }

    @Override
    public boolean addPlayer(Player player) {
        return this.addCreature(player);
    }

    @Override
    public NavigableMap<ICreature, Deque<IPoolEntry>> getPools() {
        return Collections.unmodifiableNavigableMap(this.actionPools);
    }

    @Override
    public boolean empool(ICreature key, IPoolEntry entry) {
        if (key == null || entry == null) {
            this.log(Level.WARNING, "Cannot emplace with null key or pool");
            return false;
        }
        synchronized (this.actionPools) {
            this.addCreature(key);
            Deque<IPoolEntry> queue = this.actionPools.get(key);
            if (queue != null) {
                return queue.offerLast(entry);
            }
        }
        return false;
    }

    @Override
    public ICreature keyFromContext(CommandContext ctx) {
        return ctx.getCreature();
    }

    @Override
    public boolean isReadyToFlush(ICreature key) {
        Deque<IPoolEntry> pool = this.actionPools.get(key);
        return pool == null || pool.size() >= MAX_POOLED_ACTIONS;
    }

    @Override
    public synchronized void flush() {
        final class Ordering implements Comparable<Ordering> {
            int roll;
            ICreature creature;
            Deque<IPoolEntry> entry;

            public Ordering(int roll, ICreature creature, Deque<IPoolEntry> entry) {
                this.roll = roll;
                this.creature = creature;
                this.entry = entry;
            }

            @Override
            public int compareTo(Ordering arg0) {
                if (arg0 == null) {
                    return 1;
                }
                if (arg0 == this) {
                    return 0;
                }
                return Integer.compare(arg0.roll, this.roll); // highest first
            }

        }

        final Consumer<? super Ordering> flushProcessor = ordering -> {
            Deque<IPoolEntry> poolEntries = ordering.entry;
            this.log(Level.FINEST, () -> String.format("Flush Initiative: %d, Actions: %d, Creature: %s", ordering.roll,
                    poolEntries != null ? poolEntries.size() : 0, ordering.creature));
            if (!this.hasRunningThread("flushProcessor")) {
                this.log(Level.FINE,
                        () -> String.format("The battle is over but %s still tried to go!", ordering.creature));
                return;
            }
            if (ordering.creature == null || (!ordering.creature.isAlive() && ordering.creature.isInBattle())) {
                this.log(Level.WARNING, () -> String
                        .format("Creature %s is dead or not in battle, cannot perform action", ordering.creature));
                return;
            }
            if (poolEntries != null && poolEntries.size() > 0) {
                while (poolEntries.size() > 0 && ordering.creature.isAlive() && ordering.creature.isInBattle()) {
                    IPoolEntry poolEntry = poolEntries.pollFirst();
                    if (poolEntry != null) {
                        this.handleFlushChain(poolEntry.getContext(), poolEntry.getCommand());
                        poolEntry.getContext()
                                .receive(BattleRoundEvent.getBuilder().setNeedSubmission(RoundAcceptance.PERFORMED));
                    }
                }
            } else {
                Set<CreatureEffect> penalties = this.calculateWastePenalty(ordering.creature);
                if (ordering.creature != null && penalties != null && penalties.size() > 0) {
                    this.log(Level.INFO,
                            () -> String.format("Penalties: %s, earned by Creature: %s", penalties, ordering.creature));
                    for (Iterator<CreatureEffect> effectIterator = penalties.iterator(); effectIterator.hasNext()
                            && ordering.creature.isAlive();) {
                        this.announce(ordering.creature.applyEffect(effectIterator.next()));
                    }
                    ordering.creature
                            .announce(BattleRoundEvent.getBuilder().setNeedSubmission(RoundAcceptance.PERFORMED));
                }
            }
        };

        synchronized (this.actionPools) {
            this.log(Level.FINER, "Now flushing actionpool");
            this.actionPools.entrySet().stream()
                    .map(entry -> new Ordering(entry.getKey().check(Attributes.DEX).getRoll(), entry.getKey(),
                            entry.getValue()))
                    .sorted().forEachOrdered(flushProcessor);
        }
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
    public boolean basicAddCreature(ICreature c) {
        synchronized (this.actionPools) {
            if (c != null && !c.isInBattle() && !this.hasCreature(c)) {
                if (this.actionPools.putIfAbsent(c, new LinkedBlockingDeque<>(MAX_POOLED_ACTIONS)) == null) {
                    c.addSubArea(SubAreaSort.BATTLE);
                    c.setSuccessor(this);
                    this.battleStats.initialize(this.getCreatures());
                    BattleJoinedEvent.Builder joinedMessage = BattleJoinedEvent.getBuilder().setJoiner(c).setBroacast();// new
                                                                                                                        // JoinBattleMessage(c,
                                                                                                                        // this.isBattleOngoing(),
                                                                                                                        // false);
                    synchronized (this.roundThread) {
                        boolean ongoing = this.hasRunningThread("addCreature()");
                        joinedMessage.setOngoing(ongoing);
                        if (ongoing) {
                            RoundThread thread = this.getRoundThread();
                            if (thread != null) {
                                thread.register(c);
                            }
                        }
                    }
                    ICreature.eventAccepter.accept(c, joinedMessage.setNotBroadcast().Build());
                    if (this.area != null) {
                        this.area.announce(joinedMessage.Build(), c);
                    } else {
                        this.announce(joinedMessage.Build(), c);
                    }

                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public boolean basicRemoveCreature(ICreature c) {
        if (c == null) {
            return false;
        }
        final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        this.clearCreatures(creature -> creature != null && creature.equals(c), true, creature -> {
            this.battleStats.remove(creature.getName());
            atomicBoolean.set(true);
        });
        return atomicBoolean.get();
    }

    private boolean checkCompetingFactionsPresent(final String whochecks) {
        Collection<ICreature> battlers = this.getCreatures();
        if (battlers == null || battlers.size() <= 1) {
            this.log(Level.FINER,
                    () -> String.format("%s Checking for competing factions ... No or too few battlers", whochecks));
            return false;
        }

        HashMap<CreatureFaction, Integer> factionCounts = new HashMap<>();
        for (ICreature creature : battlers) {
            if (creature == null) {
                continue;
            }
            CreatureFaction thatone = creature.getFaction();
            if (factionCounts.containsKey(thatone)) {
                factionCounts.merge(thatone, 1, (a, b) -> a + b);
            } else {
                factionCounts.put(thatone, 1);
            }
        }
        this.log(Level.FINER, () -> {
            StringJoiner sj = new StringJoiner(" ", String.format("%s Checking for competing factions...", whochecks),
                    "").setEmptyValue("No factions found");
            if (factionCounts.size() > 0) {
                sj.add("Factions found:");
                for (Map.Entry<CreatureFaction, Integer> entry : factionCounts.entrySet()) {
                    sj.add(String.format("%s %d", entry.getKey(), entry.getValue()));
                }
            }
            return sj.toString();
        });
        return CreatureFaction.hasCompetitors(factionCounts.keySet());
    }

    @Override
    public RoundThread instigate(ICreature instigator, Collection<ICreature> victims) {
        synchronized (this.roundThread) {
            RoundThread curThread = this.getRoundThread();
            if (curThread == null || !curThread.getIsRunning()) {
                this.log(Level.FINER, () -> String.format("%s starts a fight", instigator.getName()));
                this.battleStats.reset();
                this.addCreature(instigator);
                if (victims != null) {
                    for (ICreature c : victims) {
                        this.addCreature(c);
                        CreatureFaction.checkAndHandleTurnRenegade(instigator, c, this.area);
                    }
                }
                BattleStartedEvent.Builder startMessage = BattleStartedEvent.getBuilder().setInstigator(instigator)
                        .setBroacast();
                if (this.area != null) {
                    this.area.announce(startMessage.Build());
                }
                this.announce(startMessage.setNotBroadcast().Build());
                // if someone started a fight, no need to prompt them for their turn
                BattleRoundThread thread = new BattleRoundThread();
                this.log(Level.INFO, "Starting thread");
                thread.start();
                this.roundThread.set(thread);
            } else {
                this.log(Level.WARNING,
                        () -> String.format("%s tried to start an already started fight", instigator.getName()));
            }
            return this.roundThread.get();
        }
    }

    public void endBattle() {
        this.log(Level.INFO, "Ending battle");
        BattleOverEvent.Builder foverBuilder = BattleOverEvent.getBuilder().setNotBroadcast();
        this.announce(foverBuilder.Build());
        if (this.area != null) {
            this.area.announce(foverBuilder.setBroacast().Build());
        }
        synchronized (this.roundThread) {
            RoundThread thread = this.roundThread.get();
            if (thread != null) {
                thread.killIt();
            }
            this.roundThread.set(null);
        }
        int participants = Integer.min(
                (int) this.getCreatures().stream().filter(creature -> creature != null && creature.isAlive()).count(),
                1);
        int xpFromDeath = this.battleStats.getDeadXP() / participants;
        this.clearCreatures(creature -> true, false, creature -> {
            if (creature == null) {
                return;
            }
            if (!creature.isAlive()) {
                if (this.area != null) {
                    this.area.onCreatureDeath(creature);
                }
            } else {
                int xpForCreature = xpFromDeath;
                BattleStatRecord record = this.battleStats.getRecord(creature.getName());
                if (record != null) {
                    xpForCreature += record.getXPEarned();
                }
                creature.updateXp(xpForCreature);
            }
        });
        this.battleStats.reset();
    }

    @Override
    public boolean onCreatureDeath(ICreature creature) {
        if (creature == null || creature.isAlive()) {
            return false;
        }
        RoundThread thread = this.getRoundThread();
        if (thread != null && thread.isAlive()) {
            this.log(Level.FINE,
                    () -> String.format("Event onCreatureDeath(%s) ignored until clearDead() sweeps", creature));
        } else {
            this.clearCreatures(c -> c != null && creature.equals(c), true,
                    this.area != null ? c -> this.area.onCreatureDeath(c) : null);
        }
        return true;
    }

    private void clearCreatures(Predicate<ICreature> clearPredicate, boolean firstOnly, Consumer<ICreature> callback) {
        if (clearPredicate == null) {
            return;
        }
        synchronized (this.actionPools) {
            for (Iterator<ICreature> iterator = this.actionPools.keySet().iterator(); iterator.hasNext();) {
                ICreature creature = iterator.next();
                if (creature == null) {
                    iterator.remove();
                } else if (clearPredicate.test(creature)) {
                    iterator.remove();
                    creature.removeSubArea(SubAreaSort.BATTLE);
                    BattleManager.removalSuccessorSet(this, creature);
                    if (!creature.isAlive()) {
                        this.battleStats.setDead(creature.getName(),
                                creature.getStats().getOrDefault(Stats.XPWORTH, 1));
                    }
                    synchronized (this.roundThread) {
                        RoundThread thread = this.getRoundThread();
                        if (thread != null && thread.isAlive()) {
                            thread.arriveAndDeregister(creature);
                        }
                    }
                    if (callback != null) {
                        callback.accept(creature);
                    }
                    if (firstOnly) {
                        return;
                    }
                }
            }
        }
    }

    private void clearDead() {
        synchronized (this.actionPools) {
            this.log(Level.FINE, () -> "Clearing the dead");
            this.clearCreatures(creature -> creature != null && !creature.isAlive(), false,
                    this.area != null ? creature -> this.area.onCreatureDeath(creature) : null);
        }
    }

    /**
     * Calculates a penalty for if player does not respond. Scales to the level of
     * the player.
     * 
     * @param waster
     * @return Set<CreatureEffect>
     */
    private Set<CreatureEffect> calculateWastePenalty(ICreature waster) {
        // int penalty = -1;
        // if (waster.getVocation() != null) {
        // int wasterLevel = waster.getVocation().getLevel();
        // penalty += wasterLevel > 0 ? -1 * wasterLevel : wasterLevel;
        // }
        // CreatureEffectSource source = new CreatureEffectSource("Turn Waste Penalty",
        // new EffectPersistence(TickType.INSTANT), null, "A consequence for wasting a
        // turn", false)
        // .addStatChange(Stats.CURRENTHP, penalty).addStatChange(Stats.MAXHP, penalty /
        // 2);
        return Set.of();
    }

    @Override
    public String toString() {
        StringBuilder builder2 = new StringBuilder();
        builder2.append("BattleManager [participants=").append(this.actionPools.keySet()).append(", room=").append(area)
                .append(", ongoing=").append(this.hasRunningThread("toString()")).append("]");
        return builder2.toString();
    }

    @Override
    public SeeEvent produceMessage(SeeEvent.ABuilder<?> seeMessage) {
        if (seeMessage == null) {
            seeMessage = SeeEvent.getBuilder().setExaminable(this);
        }
        for (final ICreature creature : this.getCreatures()) {
            if (creature == null) {
                continue;
            }
            seeMessage.addSeen("Battling", creature);
        }
        return super.produceMessage(seeMessage);
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder();
        if (this.hasRunningThread("printDescription()")) {
            sb.append("The battle is on! ");
        } else {
            sb.append("There is no fight right now. ");
        }
        return sb.toString();
    }

    @Override
    public Map<AMessageType, CommandHandler> getCommands(CommandContext ctx) {
        if (this.cmds == null) {
            this.cmds = this.buildCommands();
        }
        return Collections.unmodifiableMap(this.cmds);
    }

    public interface BattleManagerCommandHandler extends SubAreaCommandHandler {

        @Override
        default boolean isEnabled(CommandContext ctx) {
            if (ctx == null) {
                return false;
            }
            ICreature creature = ctx.getCreature();
            if (creature == null || !creature.isAlive()) {
                return false;
            }
            final EnumSet<SubAreaSort> cSubs = ctx.getCreature().getSubAreaSorts();
            if (cSubs == null || cSubs.isEmpty()) {
                return false;
            }
            for (final SubAreaSort sort : cSubs) {
                if (!ctx.hasSubAreaSort(sort)) {
                    return false;
                }
            }
            final SubArea first = this.firstSubArea(ctx);
            return first.getSubAreaSort() == SubAreaSort.BATTLE;
        }

    }

    private class BattleStatsHandler implements BattleManagerCommandHandler {

        private final static String helpString = "\"stats\" Retrieves the statistics about the current battle.";

        @Override
        public AMessageType getHandleType() {
            return AMessageType.STATS;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(BattleStatsHandler.helpString);
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            ctx.receive(BattleStatsRequestedEvent.getBuilder()
                    .addRecords(BattleManager.this.battleStats.getBattleStatSet(BattleStatsQuery.ONLY_LIVING))
                    .setNotBroadcast());
            return ctx.handled();
        }

        @Override
        public CommandChainHandler getChainHandler(CommandContext ctx) {
            return BattleManager.this;
        }
    }

    public interface PooledBattleManagerCommandHandler extends PooledSubAreaCommandHandler {
        static final EnumMap<AMessageType, CommandHandler> pooledBattleManagerCommandHandlers = new EnumMap<>(
                Map.of(AMessageType.ATTACK, new BattleAttackHandler(), AMessageType.GO, new BattleGoHandler(),
                        AMessageType.PASS, new BattlePassHandler(), AMessageType.USE, new BattleUseHandler(),
                        AMessageType.CAST, new BattleCastHandler()));

        default SubArea firstSubArea(CommandContext ctx) {
            for (final SubArea subArea : ctx.getSubAreas()) {
                if (subArea != null) {
                    return subArea;
                }
            }
            return null;
        }

        @Override
        default boolean isEnabled(CommandContext ctx) {
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
            final SubArea first = this.firstSubArea(ctx);
            if (first.getSubAreaSort() != SubAreaSort.BATTLE) {
                return false;
            }
            return true;
        }

        @Override
        default boolean isPoolingEnabled(CommandContext ctx) {
            final SubArea first = this.firstSubArea(ctx);
            if (first == null || first.getSubAreaSort() != SubAreaSort.BATTLE) {
                return false;
            }
            return this.isEnabled(ctx) && first.hasRunningThread(this.getClass().getName() + "::isPoolingEnabled(ctx)");
        }

        @Override
        default boolean onEmpool(CommandContext ctx, boolean empoolResult) {
            RoundThread thread = ctx.getSubAreaForSort(SubAreaSort.BATTLE).getRoundThread();
            BattleRoundEvent.Builder roundMessage = BattleRoundEvent.getBuilder().setAboutCreature(ctx.getCreature())
                    .setNotBroadcast()
                    .setNeedSubmission(empoolResult ? RoundAcceptance.ACCEPTED : RoundAcceptance.REJECTED);
            if (thread != null) {
                synchronized (thread) {
                    roundMessage.setRoundCount(thread.getPhase());
                    ctx.receive(roundMessage);
                    if (empoolResult) {
                        thread.arrive(ctx.getCreature());
                    }
                }
            } else {
                ctx.receive(roundMessage);
            }
            if (RoundAcceptance.ACCEPTED.equals(roundMessage.getNeedSubmission())) {
                final SubArea battleManager = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
                if (battleManager != null) {
                    battleManager.announce(roundMessage.setBroacast(), ctx.getCreature());
                }
            }
            return empoolResult;
        }

        @Override
        default PooledMessageChainHandler<?> getPooledChainHandler(CommandContext ctx) {
            return ctx.getSubAreaForSort(SubAreaSort.BATTLE);
        }

        @Override
        default CommandChainHandler getChainHandler(CommandContext ctx) {
            return this.getPooledChainHandler(ctx);
        }
    }

    /**
     * Calls for reinforcements for the battling creatures. If the targetCreature is
     * a renegade or has no faction, it cannot call for reinforcements.
     * 
     */
    public void callReinforcements() {
        final Collection<ICreature> battleCreatures = this.getCreatures();
        if (battleCreatures == null || battleCreatures.size() == 0) {
            this.log(Level.INFO, "No creatures retrieved from battle");
            return;
        }
        final Function<ICreature, CreatureFaction> factionGetter = creature -> {
            CreatureFaction faction = creature.getFaction();
            if (faction == null) {
                return CreatureFaction.RENEGADE;
            }
            return faction;
        };
        final Map<Boolean, EnumMap<CreatureFaction, TreeSet<ICreature>>> coalated = this.area.getCreatures().stream()
                .filter(creature -> creature != null)
                .collect(Collectors.partitioningBy(creature -> battleCreatures.contains(creature),
                        Collectors.groupingBy(factionGetter, () -> new EnumMap<>(CreatureFaction.class),
                                Collectors.toCollection(() -> new TreeSet<>()))));

        this.log(Level.FINEST, () -> String.format("Creature distribution: %s", coalated));
        if (coalated == null || coalated.isEmpty()) {
            return;
        }

        final EnumMap<CreatureFaction, TreeSet<ICreature>> inBattle = coalated.get(true);
        if (inBattle == null || inBattle.size() == 0) {
            this.log(Level.INFO, "No creatures found IN battle!");
            return;
        }

        final EnumMap<CreatureFaction, TreeSet<ICreature>> outBattle = coalated.get(false);
        if (outBattle == null || outBattle.size() == 0) {
            this.log(Level.INFO, "No creatures found that are NOT in battle!");
            return;
        }

        FactionReinforcementsCallEvent.Builder reBuilder = FactionReinforcementsCallEvent.getBuilder()
                .setNotBroadcast();

        for (CreatureFaction faction : CreatureFaction.values()) {
            final TreeSet<ICreature> factionInCreatures = inBattle.getOrDefault(faction, new TreeSet<>());
            if (factionInCreatures.isEmpty()) {
                continue;
            }
            if (CreatureFaction.RENEGADE.equals(faction)) {
                // tell all the renegades that no help is coming for them
                for (ICreature creature : factionInCreatures) {
                    ICreature.eventAccepter.accept(creature, reBuilder.setNotBroadcast().setCaller(creature).Build());
                }
                continue;
            }
            final TreeSet<ICreature> factionOutCreatures = outBattle.getOrDefault(faction, new TreeSet<>());
            if (factionOutCreatures.isEmpty()) {
                continue;
            }
            Area.eventAccepter.accept(this.area, reBuilder.setBroacast().setCaller(factionInCreatures.first()).Build());
            for (ICreature creature : factionOutCreatures) {
                this.addCreature(creature);
            }
        }

    }

    @Override
    public Collection<GameEventProcessor> getGameEventProcessors() {
        Collection<GameEventProcessor> messengers = super.getGameEventProcessors();
        messengers.add(this.battleStats);
        return messengers;
    }

    @Override
    public Consumer<GameEvent> getAcceptHook() {
        return (event) -> {
            if (event == null) {
                return;
            }
            this.log(Level.FINEST, () -> String.format("Received message %s, announcing", event.getUuid()));
            this.announceDirect(event, this.getGameEventProcessors());
        };
    }

}
