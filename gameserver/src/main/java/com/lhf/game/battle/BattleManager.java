package com.lhf.game.battle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
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

import com.lhf.game.EffectResistance;
import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.game.battle.BattleStats.BattleStatsQuery;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Item;
import com.lhf.game.item.Usable;
import com.lhf.game.item.Weapon;
import com.lhf.game.map.Area;
import com.lhf.game.map.SubArea;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.PooledMessageChainHandler;
import com.lhf.messages.events.BadTargetSelectedEvent;
import com.lhf.messages.events.BadTargetSelectedEvent.BadTargetOption;
import com.lhf.messages.events.BattleCreatureFledEvent;
import com.lhf.messages.events.BattleJoinedEvent;
import com.lhf.messages.events.BattleOverEvent;
import com.lhf.messages.events.BattleRoundEvent;
import com.lhf.messages.events.BattleRoundEvent.RoundAcceptance;
import com.lhf.messages.events.BattleStartedEvent;
import com.lhf.messages.events.BattleStatsRequestedEvent;
import com.lhf.messages.events.FactionReinforcementsCallEvent;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.ItemNotPossessedEvent;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.events.TargetDefendedEvent;
import com.lhf.messages.in.AttackMessage;
import com.lhf.messages.in.GoMessage;
import com.lhf.messages.in.PassMessage;
import com.lhf.messages.in.UseMessage;
import com.lhf.server.client.user.UserID;

public class BattleManager extends SubArea {
    private BattleStats battleStats;

    public static class Builder extends SubAreaBuilder<BattleManager, Builder> {

        public static Builder getInstance() {
            return new Builder().setAllowCasting(true);
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public SubAreaSort getSubAreaSort() {
            return SubAreaSort.BATTLE;
        }

        @Override
        public BattleManager build(Area area) {
            return new BattleManager(this, area);
        }

    }

    protected class BattleRoundThread extends RoundThread {

        protected BattleRoundThread() {
            super(BattleManager.this.getName());
        }

        @Override
        public void onThreadStart() {
            // does nothing so far
        }

        @Override
        public void onRoundStart() {
            Map<Boolean, Set<ICreature>> partitions = BattleManager.this.getCreatures().stream()
                    .filter(c -> c != null).collect(Collectors.partitioningBy(
                            creature -> BattleManager.this.isReadyToFlush(creature),
                            Collectors.toSet()));
            BattleManager.this.announce(
                    BattleRoundEvent.getBuilder().setNeedSubmission(BattleRoundEvent.RoundAcceptance.NEEDED)
                            .setNotBroadcast().setRoundCount(this.getPhase())
                            .Build(),
                    partitions.getOrDefault(true, null));
        }

        @Override
        public synchronized void onRoundEnd() {
            this.logger.log(Level.FINE, "Ending Round");
            BattleManager.this.flush();
            BattleManager.this.battleStats.initialize(getCreatures());
            BattleManager.this.clearDead();
            if (!BattleManager.this.checkCompetingFactionsPresent("endRound()")) {
                this.killIt();
                return;
            }
            BattleManager.this.callReinforcements();
        }

        @Override
        public void onThreadEnd() {
            // does nothing so far
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

        public synchronized boolean getIsRunning() {
            return super.getIsRunning();
        }

    }

    public BattleManager(Builder builder, Area room) {
        super(builder, room);
        this.battleStats = new BattleStats().initialize(this.getCreatures());
    }

    @Override
    protected EnumMap<CommandMessage, CommandHandler> buildCommands() {
        EnumMap<CommandMessage, CommandHandler> cmds = new EnumMap<>(CommandMessage.class);
        cmds.put(CommandMessage.SEE, new SeeHandler());
        cmds.put(CommandMessage.GO, new GoHandler());
        cmds.put(CommandMessage.PASS, new PassHandler());
        cmds.put(CommandMessage.USE, new UseHandler());
        cmds.put(CommandMessage.STATS, new StatsHandler());
        cmds.put(CommandMessage.ATTACK, new AttackHandler());
        cmds.put(CommandMessage.EXIT, new SubAreaExitHandler());
        cmds.put(CommandMessage.SAY, new SubAreaSayHandler());
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
            this.log(Level.FINEST, () -> String.format("Flush Initiative: %d, Actions: %d, Creature: %s",
                    ordering.roll, poolEntries != null ? poolEntries.size() : 0, ordering.creature));
            if (!this.hasRunningThread("flushProcessor")) {
                this.log(Level.FINE,
                        () -> String.format("The battle is over but %s still tried to go!", ordering.creature));
                return;
            }
            if (ordering.creature == null || (!ordering.creature.isAlive() && ordering.creature.isInBattle())) {
                this.log(Level.WARNING,
                        () -> String.format("Creature %s is dead or not in battle, cannot perform action",
                                ordering.creature));
                return;
            }
            if (poolEntries != null && poolEntries.size() > 0) {
                while (poolEntries.size() > 0 && ordering.creature.isAlive() && ordering.creature.isInBattle()) {
                    IPoolEntry poolEntry = poolEntries.pollFirst();
                    if (poolEntry != null) {
                        this.handleFlushChain(poolEntry.getContext(), poolEntry.getCommand());
                    }
                }
            } else {
                Set<CreatureEffect> penalties = this.calculateWastePenalty(ordering.creature);
                if (ordering.creature != null && penalties != null && penalties.size() > 0) {
                    this.log(Level.INFO, () -> String.format("Penalties: %s, earned by Creature: %s", penalties,
                            ordering.creature));
                    for (Iterator<CreatureEffect> effectIterator = penalties.iterator(); effectIterator.hasNext()
                            && ordering.creature.isAlive();) {
                        this.announce(ordering.creature.applyEffect(effectIterator.next()));
                    }
                }
            }
        };

        synchronized (this.actionPools) {
            this.log(Level.FINER, "Now flushing actionpool");
            this.actionPools.entrySet().stream().map(
                    entry -> new Ordering(entry.getKey().check(Attributes.DEX).getRoll(), entry.getKey(),
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
    public synchronized boolean basicAddCreature(ICreature c) {
        synchronized (this.actionPools) {
            if (c != null && !c.isInBattle() && !this.hasCreature(c)) {
                if (this.actionPools.putIfAbsent(c, new LinkedBlockingDeque<>(MAX_POOLED_ACTIONS)) == null) {
                    c.addSubArea(SubAreaSort.BATTLE);
                    c.setSuccessor(this);
                    this.battleStats.initialize(this.getCreatures());
                    boolean ongoing = this.hasRunningThread("addCreature()");
                    BattleJoinedEvent.Builder joinedMessage = BattleJoinedEvent.getBuilder().setJoiner(c)
                            .setOngoing(ongoing).setBroacast();// new JoinBattleMessage(c, this.isBattleOngoing(),
                                                               // false);
                    if (this.area != null) {
                        this.area.announce(joinedMessage.Build(), c);
                    } else {
                        this.announce(joinedMessage.Build(), c);
                    }
                    ICreature.eventAccepter.accept(c, joinedMessage.setNotBroadcast().Build());
                    if (ongoing) {
                        RoundThread thread = this.getRoundThread();
                        if (thread != null) {
                            synchronized (thread) {
                                thread.register(c);
                            }
                        }
                    }
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public synchronized boolean basicRemoveCreature(ICreature c) {
        if (c == null) {
            return false;
        }
        final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        this.clearCreatures(creature -> creature != null && creature.equals(c), true,
                creature -> {
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
            CreatureFaction thatone = creature.getFaction();
            if (factionCounts.containsKey(thatone)) {
                factionCounts.put(thatone, factionCounts.get(thatone) + 1);
            } else {
                factionCounts.put(thatone, 1);
            }
        }
        this.log(Level.FINER, () -> {
            StringJoiner sj = new StringJoiner(" ", String.format("%s Checking for competing factions...", whochecks),
                    "")
                    .setEmptyValue("No factions found");
            if (factionCounts.size() > 0) {
                sj.add("Factions found:");
                for (Map.Entry<CreatureFaction, Integer> entry : factionCounts.entrySet()) {
                    sj.add(String.format("%s %d", entry.getKey(), entry.getValue()));
                }
            }
            return sj.toString();
        });
        if (factionCounts.containsKey(CreatureFaction.RENEGADE)) {
            return true;
        }
        if (factionCounts.keySet().size() == 1) {
            return false;
        }
        if (factionCounts.containsKey(CreatureFaction.PLAYER)) {
            return true;
        }
        return false;
    }

    @Override
    public synchronized RoundThread instigate(ICreature instigator, Collection<ICreature> victims) {
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
            this.log(Level.WARNING, () -> String.format("%s tried to start an already started fight",
                    instigator.getName()));
        }
        return this.roundThread.get();
    }

    public void endBattle() {
        this.log(Level.INFO, "Ending battle");
        BattleOverEvent.Builder foverBuilder = BattleOverEvent.getBuilder().setNotBroadcast();
        this.announce(foverBuilder.Build());
        if (this.area != null) {
            this.area.announce(foverBuilder.setBroacast().Build());
        }
        RoundThread thread = this.roundThread.get();
        if (thread != null) {
            synchronized (thread) {
                thread.killIt();
            }
        }
        this.roundThread.set(null);
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
                    RoundThread thread = this.getRoundThread();
                    if (thread != null && thread.isAlive()) {
                        synchronized (thread) {
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
     * Calculates a penalty for if player does not respond.
     * Scales to the level of the player.
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
    public SeeEvent produceMessage(SeeEvent.Builder seeMessage) {
        if (seeMessage == null) {
            seeMessage = SeeEvent.getBuilder().setExaminable(this);
        }
        for (final ICreature creature : this.getCreatures()) {
            if (creature == null || !creature.getSubAreaSorts().contains(this.getSubAreaSort())) {
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
            // TODO: round count?
        } else {
            sb.append("There is no fight right now. ");
        }
        return sb.toString();
    }

    @Override
    public Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx) {
        if (this.cmds == null) {
            this.cmds = this.buildCommands();
        }
        return Collections.unmodifiableMap(this.cmds);
    }

    public interface BattleManagerCommandHandler extends SubAreaCommandHandler {
        static final Predicate<CommandContext> defaultBattlePredicate = SubAreaCommandHandler.defaultSubAreaPredicate
                .and(ctx -> ctx.hasSubAreaSort(SubAreaSort.BATTLE));

    }

    public interface PooledBattleManagerCommandHandler extends PooledCommandHandler {
        static final Predicate<CommandContext> defaultTurnPredicate = BattleManagerCommandHandler.defaultBattlePredicate
                .and(ctx -> ctx.getSubAreaForSort(SubAreaSort.BATTLE).hasRunningThread(".defaultTurnPredicate"));

        @Override
        public default Predicate<CommandContext> getPoolingPredicate() {
            return PooledBattleManagerCommandHandler.defaultTurnPredicate;
        }

        @Override
        default boolean onEmpool(CommandContext ctx, boolean empoolResult) {
            RoundThread thread = ctx.getSubAreaForSort(SubAreaSort.BATTLE).getRoundThread();
            BattleRoundEvent.Builder roundMessage = BattleRoundEvent.getBuilder()
                    .setAboutCreature(ctx.getCreature()).setNotBroadcast()
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
            return empoolResult;
        }

        @Override
        default PooledMessageChainHandler<?> getPooledChainHandler(CommandContext ctx) {
            return ctx.getSubAreaForSort(SubAreaSort.BATTLE);
        }
    }

    private class StatsHandler implements BattleManagerCommandHandler {
        private final static String helpString = "\"stats\" Retrieves the statistics about the current battle.";

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.STATS;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(StatsHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return StatsHandler.defaultBattlePredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            ctx.receive(BattleStatsRequestedEvent.getBuilder()
                    .addRecords(BattleManager.this.battleStats.getBattleStatSet(BattleStatsQuery.ONLY_LIVING))
                    .setNotBroadcast());
            return ctx.handled();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return BattleManager.this;
        }
    }

    private class UseHandler implements PooledBattleManagerCommandHandler {
        private final static Predicate<CommandContext> enabledPredicate = UseHandler.defaultTurnPredicate.and(
                ctx -> ctx.getCreature().getItems().stream().anyMatch(item -> item != null && item instanceof Usable));
        private final static String helpString = new StringJoiner(" ")
                .add("\"use [itemname]\"").add("Uses an item that you have on yourself, if applicable.")
                .add("Like \"use potion\"").add("\r\n")
                .add("\"use [itemname] on [otherthing]\"")
                .add("Uses an item that you have on something or someone else, if applicable.")
                .add("Like \"use potion on Bob\"").toString();

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.USE;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(UseHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return UseHandler.enabledPredicate;
        }

        @Override
        public Reply flushHandle(CommandContext ctx, Command cmd) {
            // TODO: #127 test me!
            if (cmd != null && cmd.getType() == this.getHandleType() && cmd instanceof UseMessage useMessage) {
                Reply reply = CommandChainHandler.passUpChain(BattleManager.this, ctx, useMessage);
                return reply;
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return BattleManager.this;
        }

    }

    private class PassHandler implements PooledBattleManagerCommandHandler {

        private static String helpString = "\"pass\" Skips your turn in battle!";

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.PASS;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(PassHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return PassHandler.defaultTurnPredicate;
        }

        @Override
        public Reply flushHandle(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == this.getHandleType() && cmd instanceof PassMessage passMessage) {
                return ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return BattleManager.this;
        }

    }

    private class SeeHandler extends SubAreaSeeHandler {
        private static final String helpString = "\"see\" Will give you some information about the battle.\r\n";

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(SeeHandler.helpString);
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return BattleManager.this;
        }

    }

    private class GoHandler implements PooledBattleManagerCommandHandler {
        private final static String helpString = "\"go [direction]\" Try to move in the desired direction and flee the battle, if that direction exists.  Like \"go east\"";
        private final static Predicate<CommandContext> enabledPredicate = BattleManagerCommandHandler.defaultBattlePredicate
                .and(ctx -> {
                    SubArea bm = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
                    CommandChainHandler chainHandler = bm.getSuccessor();
                    CommandContext copyContext = ctx.copy();

                    // this whole block means: if someone further up in the chain has GO, then I
                    // have GO, else not
                    while (chainHandler != null) {
                        copyContext = chainHandler.addSelfToContext(copyContext);
                        Map<CommandMessage, CommandHandler> handlers = chainHandler.getCommands(copyContext);
                        if (handlers != null) {
                            CommandHandler handler = handlers.get(CommandMessage.GO);
                            if (handler != null && handler.isEnabled(copyContext)) {
                                return true;
                            }
                        }
                        chainHandler = chainHandler.getSuccessor();
                    }
                    return false;
                });

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.GO;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(GoHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return GoHandler.enabledPredicate;
        }

        @Override
        public Reply flushHandle(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.GO && cmd instanceof GoMessage goMessage) {
                Integer check = 10 + BattleManager.this.actionPools.size();
                MultiRollResult result = ctx.getCreature().check(Attributes.DEX);
                BattleCreatureFledEvent.Builder builder = BattleCreatureFledEvent.getBuilder()
                        .setRunner(ctx.getCreature())
                        .setRoll(result);
                Reply reply = null;
                if (result.getRoll() >= check) {
                    reply = CommandChainHandler.passUpChain(BattleManager.this, ctx, goMessage);
                }
                if (BattleManager.this.hasCreature(ctx.getCreature())) { // if it is still here, it failed to flee
                    builder.setFled(false);
                    ctx.receive(builder.setFled(false).setNotBroadcast().Build());
                    if (BattleManager.this.area != null) {
                        BattleManager.this.area.announce(builder.setBroacast().Build(), ctx.getCreature());
                    } else {
                        BattleManager.this.announce(builder.setBroacast().Build(), ctx.getCreature());
                    }
                } else {
                    builder.setFled(true).setBroacast();
                    if (BattleManager.this.area != null) {
                        BattleManager.this.area.announce(builder.Build(), ctx.getCreature());
                    } else {
                        BattleManager.this.announce(builder.Build(), ctx.getCreature());
                    }
                }
                return reply != null ? reply.resolve() : ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return BattleManager.this;
        }

    }

    /**
     * Collect the targeted creatures from the room.
     * If it is unclear which target is meant, it will skip that name.
     * If the self is targeted, then it will be skipped.
     * 
     * @param attacker Creature who selected the targets
     * @param names    names of the targets
     * @return Best effort list of Creatures, possibly size 0
     */
    private List<ICreature> collectTargetsFromRoom(ICreature attacker, List<String> names) {
        List<ICreature> targets = new ArrayList<>();
        BadTargetSelectedEvent.Builder btMessBuilder = BadTargetSelectedEvent.getBuilder().setNotBroadcast();
        if (names == null || names.size() == 0) {
            return targets;
        }
        for (String targetName : names) {
            btMessBuilder.setBadTarget(targetName);
            List<ICreature> possTargets = new ArrayList<>(this.area.getCreaturesLike(targetName));
            if (possTargets.size() == 1) {
                ICreature targeted = possTargets.get(0);
                if (targeted.equals(attacker)) {
                    ICreature.eventAccepter.accept(attacker, btMessBuilder.setBde(BadTargetOption.SELF).Build());
                    continue; // go to next name
                }
                targets.add(targeted);
            } else {
                btMessBuilder.setPossibleTargets(possTargets);
                if (possTargets.size() == 0) {
                    ICreature.eventAccepter.accept(attacker, btMessBuilder.setBde(BadTargetOption.DNE).Build());
                } else {
                    ICreature.eventAccepter.accept(attacker, btMessBuilder.setBde(BadTargetOption.UNCLEAR).Build());
                }
                continue; // go to next name
            }
        }
        return targets;
    }

    private class AttackHandler implements PooledBattleManagerCommandHandler {
        private final static String helpString = new StringJoiner(" ")
                .add("\"attack [name]\"").add("Attacks a creature").add("\r\n")
                .add("\"attack [name] with [weapon]\"").add("Attack the named creature with a weapon that you have.")
                .add("In the unlikely event that either the creature or the weapon's name contains 'with', enclose the name in quotation marks.")
                .toString();
        private final static Predicate<CommandContext> enabledPredicate = ICreature.CreatureCommandHandler.defaultCreaturePredicate
                .and(ctx -> {
                    SubArea bm = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
                    if (bm == null) {
                        return false;
                    }
                    if (bm.getArea() == null) {
                        return false;
                    }
                    return bm.getArea().getCreatures().size() > 1 || bm.getCreatures().size() > 1;
                });

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd == null || !(cmd instanceof AttackMessage)) {
                return ctx.failhandle();
            }
            if (!BattleManager.this.hasRunningThread("AttackHandler.handle()")) {
                ICreature attacker = ctx.getCreature();
                List<ICreature> collected = BattleManager.this.collectTargetsFromRoom(attacker,
                        ((AttackMessage) cmd).getTargets());
                if (collected == null || collected.size() == 0) {
                    ctx.receive(BadTargetSelectedEvent.getBuilder()
                            .setNotBroadcast().setBde(BadTargetOption.NOTARGET).Build());
                    return ctx.handled();
                }
                this.log(Level.FINE, "No current battle detected, starting battle");
                BattleManager.this.instigate(attacker, collected);
            } else {
                this.log(Level.FINE, "Battle detected, empooling command");
            }
            this.onEmpool(ctx, BattleManager.this.empool(ctx, cmd));
            return ctx.handled();
        }

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.ATTACK;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(AttackHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return AttackHandler.enabledPredicate;
        }

        @Override
        public boolean isEnabled(CommandContext ctx) {
            this.log(Level.FINEST, "Handling enabled check");
            boolean result = PooledBattleManagerCommandHandler.super.isEnabled(ctx);
            this.log(Level.FINEST, () -> String.format("Handling enabled: %b", result));
            return result;
        }

        @Override
        public boolean isPoolingEnabled(CommandContext ctx) {
            this.log(Level.FINEST, "Pooling enabled check");
            boolean result = PooledBattleManagerCommandHandler.super.isPoolingEnabled(ctx);
            this.log(Level.FINEST, () -> String.format("Pooling enabled: %b", result));
            return result;
        }

        /**
         * Gets a designated weapon for a creature, either by name, or by default.
         * If a name is provided, but is not found or the item found is not a weapon,
         * then return NULL.
         * 
         * @param attacker   The creature who is attacking
         * @param weaponName The name of a weapon
         * @return a weapon or NULL if the item found is not a weapon or is not found
         */
        private Weapon getDesignatedWeapon(ICreature attacker, String weaponName) {
            if (weaponName != null && weaponName.length() > 0) {
                Optional<Item> inventoryItem = attacker.getItem(weaponName);
                ItemNotPossessedEvent.Builder builder = ItemNotPossessedEvent.getBuilder().setNotBroadcast()
                        .setItemName(weaponName).setItemType(Weapon.class.getSimpleName());
                if (inventoryItem.isEmpty()) {
                    ICreature.eventAccepter.accept(attacker, builder.Build());
                    return null;
                } else if (!(inventoryItem.get() instanceof Weapon)) {
                    ICreature.eventAccepter.accept(attacker, builder.setFound(inventoryItem.get()).Build());
                    return null;
                } else {
                    return (Weapon) inventoryItem.get();
                }
            } else {
                return attacker.defaultWeapon();
            }
        }

        private void applyAttacks(ICreature attacker, Weapon weapon, Collection<ICreature> targets) {
            for (ICreature target : targets) {
                this.log(Level.FINEST,
                        () -> String.format("Applying attack from %s on %s", attacker.getName(), target.getName()));
                CreatureFaction.checkAndHandleTurnRenegade(attacker, target, BattleManager.this.area);
                if (!BattleManager.this.hasCreature(target)) {
                    BattleManager.this.addCreature(target);
                }
                Attack a = attacker.attack(weapon);
                Vocation attackerVocation = attacker.getVocation();
                if (attackerVocation != null && attackerVocation instanceof MultiAttacker) {
                    a = ((MultiAttacker) attackerVocation).modifyAttack(a, false);
                }

                for (CreatureEffect effect : a) {
                    EffectResistance resistance = effect.getResistance();
                    MultiRollResult attackerResult = null;
                    MultiRollResult targetResult = null;
                    if (resistance != null) {
                        attackerResult = resistance.actorEffort(attacker, weapon.getToHitBonus());
                        targetResult = resistance.targetEffort(target, 0);
                    }

                    if (resistance == null || targetResult == null
                            || (attackerResult != null && (attackerResult.getTotal() > targetResult.getTotal()))) {
                        GameEvent cam = target.applyEffect(effect);
                        BattleManager.this.announce(cam);
                    } else {
                        BattleManager.this
                                .announce(TargetDefendedEvent.getBuilder().setAttacker(attacker).setTarget(target)
                                        .setOffense(attackerResult)
                                        .setDefense(targetResult).Build());
                    }
                }

            }
        }

        @Override
        public Reply flushHandle(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == this.getHandleType() && cmd instanceof AttackMessage aMessage) {
                BattleManager.this.log(Level.INFO,
                        ctx.getCreature().getName() + " attempts attacking " + aMessage.getTargets());

                ICreature attacker = ctx.getCreature();

                BadTargetSelectedEvent.Builder btMessBuilder = BadTargetSelectedEvent.getBuilder()
                        .setNotBroadcast();

                if (aMessage.getNumTargets() == 0) {
                    ctx.receive(btMessBuilder.setBde(BadTargetOption.NOTARGET).Build());
                    return ctx.handled();
                }

                int numAllowedTargets = 1;
                Vocation attackerVocation = attacker.getVocation();
                if (attackerVocation != null && attackerVocation instanceof MultiAttacker) {
                    numAllowedTargets = ((MultiAttacker) attackerVocation).maxAttackCount(false);
                }

                if (aMessage.getNumTargets() > numAllowedTargets) {
                    String badTarget = aMessage.getTargets().get(numAllowedTargets);
                    ctx.receive(btMessBuilder.setBadTarget(badTarget).setBde(BadTargetOption.TOO_MANY).Build());
                    return ctx.handled();
                }

                List<ICreature> targets = BattleManager.this.collectTargetsFromRoom(attacker, aMessage.getTargets());
                if (targets == null || targets.size() == 0) {
                    ctx.receive(btMessBuilder.setBde(BadTargetOption.NOTARGET).Build());
                    return ctx.handled();
                }

                if (attackerVocation != null && attackerVocation instanceof MultiAttacker) {
                    ((MultiAttacker) attackerVocation).attackNumberOfTargets(targets.size(), false);
                }

                Weapon weapon = this.getDesignatedWeapon(attacker, aMessage.getWeapon());
                if (weapon == null) {
                    this.log(Level.SEVERE, () -> String.format("No weapon found! %s %s", ctx, cmd));
                    return ctx.handled();
                }

                this.applyAttacks(attacker, weapon, targets);
                return ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return BattleManager.this;
        }
    }

    /**
     * Calls for reinforcements for the battling creatures.
     * If the targetCreature is a renegade or has no faction, it cannot call for
     * reinforcements.
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
            this.log(Level.FINEST,
                    () -> String.format("Received message %s, announcing", event.getUuid()));
            this.announceDirect(event, this.getGameEventProcessors());
        };
    }

}
