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
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Phaser;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.lhf.game.CreatureContainer;
import com.lhf.game.EffectResistance;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.item.Item;
import com.lhf.game.item.Usable;
import com.lhf.game.item.Weapon;
import com.lhf.game.map.Area;
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageChainHandler;
import com.lhf.messages.PooledMessageChainHandler;
import com.lhf.messages.in.AttackMessage;
import com.lhf.messages.in.GoMessage;
import com.lhf.messages.in.PassMessage;
import com.lhf.messages.in.SeeMessage;
import com.lhf.messages.in.UseMessage;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;
import com.lhf.messages.out.BattleRoundMessage;
import com.lhf.messages.out.BattleRoundMessage.RoundAcceptance;
import com.lhf.messages.out.FightOverMessage;
import com.lhf.messages.out.FleeMessage;
import com.lhf.messages.out.JoinBattleMessage;
import com.lhf.messages.out.MissMessage;
import com.lhf.messages.out.NotPossessedMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.ReinforcementsCall;
import com.lhf.messages.out.RenegadeAnnouncement;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.messages.out.StartFightMessage;
import com.lhf.messages.out.StatsOutMessage;
import com.lhf.server.client.user.UserID;
import com.lhf.server.interfaces.NotNull;

public class BattleManager implements CreatureContainer, PooledMessageChainHandler<Creature> {
    private final static int MAX_POOLED_ACTIONS = 1;
    private final static int MAX_MILLISECONDS = 120000;
    private final static int DEFAULT_MILLISECONDS = 90000;
    private final int roundDurationMilliseconds;
    private AtomicReference<RoundThread> battleThread;
    private NavigableMap<Creature, Deque<IPoolEntry>> actionPools;
    private BattleStats battleStats;
    private Area room;
    private transient MessageChainHandler successor;
    private transient Map<CommandMessage, CommandHandler> cmds;
    private Logger battleLogger;
    private transient Set<UUID> sentMessage;

    public static class Builder {
        private int waitMilliseconds;
        private NavigableMap<Creature, Deque<IPoolEntry>> actionPools;

        public static Builder getInstance() {
            return new Builder();
        }

        private Builder() {
            this.waitMilliseconds = BattleManager.DEFAULT_MILLISECONDS;
            this.actionPools = new TreeMap<>();
        }

        public Builder setWaitMilliseconds(int count) {
            this.waitMilliseconds = Integer.min(BattleManager.MAX_MILLISECONDS, Integer.max(1, count));
            return this;
        }

        public Builder addCreature(Creature creature) {
            if (creature != null) {
                this.actionPools.put(creature, new LinkedBlockingDeque<>(BattleManager.MAX_POOLED_ACTIONS));
            }
            return this;
        }

        public Builder empool(Creature creature, CommandContext ctx, Command cmd) {
            if (creature != null) {
                this.addCreature(creature);
                Deque<IPoolEntry> pool = this.actionPools.get(creature);
                if (pool != null && cmd != null) {
                    pool.offerLast(new PoolEntry(ctx, cmd));
                }
            }
            return this;
        }

        public Builder clearPool() {
            this.actionPools = new TreeMap<>();
            return this;
        }

        public NavigableMap<Creature, Deque<IPoolEntry>> getActionPools() {
            return actionPools;
        }

        public BattleManager Build(Area room) {
            return new BattleManager(room, this);
        }

        public int getWaitMilliseconds() {
            return waitMilliseconds;
        }

    }

    protected class RoundThread extends Thread {
        protected AtomicBoolean isRunning;
        private final Phaser roundPhaser;
        protected Logger threadLogger;
        private final ScheduledExecutorService timerExecutor;

        protected RoundThread() {
            this.isRunning = new AtomicBoolean(false);
            this.roundPhaser = new Phaser(1); // +1 for round thread?
            this.timerExecutor = Executors.newScheduledThreadPool(1);
            this.threadLogger = Logger
                    .getLogger(this.getClass().getName() + "." + BattleManager.this.getName().replaceAll("\\W", "_"));
        }

        @Override
        public void run() {
            this.threadLogger.log(Level.INFO, "Running");
            this.isRunning.set(true);
            while (this.isRunning.get() && !this.roundPhaser.isTerminated()) {
                this.roundPhaser.bulkRegister(BattleManager.this.actionPools.size());
                this.threadLogger.log(Level.INFO, () -> String.format("Phase: %d", this.roundPhaser.getPhase()));
                BattleManager.this.announce(
                        BattleRoundMessage.getBuilder().setNeedSubmission(BattleRoundMessage.RoundAcceptance.NEEDED)
                                .setNotBroadcast().setRoundCount(this.roundPhaser.getPhase())
                                .Build());
                ScheduledFuture<?> timerFuture = timerExecutor.schedule(this::endRound,
                        BattleManager.this.getTurnWaitCount(), TimeUnit.MILLISECONDS);

                this.roundPhaser.arriveAndAwaitAdvance();

                if (!timerFuture.isDone()) {
                    this.endRound();
                    timerFuture.cancel(true);
                }

            }
            threadLogger.exiting(this.getClass().getName(), "run()");
        }

        public void endRound() {
            BattleManager.this.flush();

            BattleManager.this.clearDead();
            if (!BattleManager.this.checkCompetingFactionsPresent()) {
                this.threadLogger.log(Level.INFO, () -> String.format("No compteting factions found"));
                this.isRunning.set(false);
                threadLogger.exiting(this.getClass().getName(), "run()");
                return;
            }
        }

        public boolean getIsRunning() {
            return this.isRunning.get() && this.isAlive();
        }

        protected void killIt() {
            this.isRunning.set(false);
        }

    }

    public BattleManager(Area room, Builder builder) {
        this.actionPools = Collections.synchronizedNavigableMap(new TreeMap<>(builder.getActionPools()));
        this.battleStats = new BattleStats().initialize(builder.getActionPools().keySet());
        this.room = room;
        this.successor = this.room;
        this.roundDurationMilliseconds = builder.waitMilliseconds;
        this.sentMessage = new TreeSet<>();
        this.init();
    }

    private void init() {
        this.cmds = this.buildCommands();
        this.battleThread = new AtomicReference<BattleManager.RoundThread>(null);
        this.battleLogger = Logger.getLogger(this.getClass().getName() + "." + this.getName().replaceAll("\\W", "_"));
    }

    protected int getTurnWaitCount() {
        return this.roundDurationMilliseconds;
    }

    protected int getMaxPokesPerAction() {
        return 2;
    }

    private Map<CommandMessage, CommandHandler> buildCommands() {
        Map<CommandMessage, CommandHandler> cmds = new EnumMap<>(CommandMessage.class);
        cmds.put(CommandMessage.SEE, new SeeHandler());
        cmds.put(CommandMessage.GO, new GoHandler());
        cmds.put(CommandMessage.PASS, new PassHandler());
        cmds.put(CommandMessage.USE, new UseHandler());
        cmds.put(CommandMessage.STATS, new StatsHandler());
        cmds.put(CommandMessage.ATTACK, new AttackHandler());
        return cmds;
    }

    @Override
    public String getName() {
        return this.room != null ? this.room.getName() + " Battle" : "Battle";
    }

    @Override
    public boolean addPlayer(Player player) {
        return this.addCreature(player);
    }

    @Override
    public NavigableMap<Creature, Deque<IPoolEntry>> getPools() {
        return Collections.unmodifiableNavigableMap(this.actionPools);
    }

    @Override
    public boolean empool(Creature key, IPoolEntry entry) {
        if (key == null || entry == null) {
            this.log(Level.WARNING, "Cannot emplace with null key or pool");
            return false;
        }
        this.addCreature(key);
        Deque<IPoolEntry> queue = this.actionPools.get(key);
        if (queue != null) {
            return queue.offerLast(entry);
        }
        return false;
    }

    @Override
    public Creature keyFromContext(CommandContext ctx) {
        return ctx.getCreature();
    }

    @Override
    public boolean isReadyToFlush() {
        return this.actionPools.entrySet().stream().allMatch(entry -> {
            Deque<IPoolEntry> pool = entry.getValue();
            return pool == null || pool.size() >= 1; // maybe check for how many attacks can be made?
        });
    }

    @Override
    public synchronized void flush() {
        final class Ordering implements Comparable<Ordering> {
            int roll;
            Creature creature;
            Deque<IPoolEntry> entry;

            public Ordering(int roll, Creature creature, Deque<IPoolEntry> entry) {
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
                return Integer.compare(this.roll, arg0.roll);
            }

        }
        this.log(Level.FINER, "Now flushing actionpool");
        this.actionPools.entrySet().stream().map(
                entry -> new Ordering(entry.getKey().check(Attributes.DEX).getRoll(), entry.getKey(), entry.getValue()))
                .sorted().forEachOrdered(ordering -> {
                    Deque<IPoolEntry> poolEntries = ordering.entry;
                    this.log(Level.FINEST, () -> String.format("Flush Initiative: %d, Actions: %d, Creature: %s",
                            ordering.roll, poolEntries != null ? poolEntries.size() : 0, ordering.creature));
                    if (poolEntries != null && poolEntries.size() > 0) {
                        while (poolEntries.size() > 0) {
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
                            for (CreatureEffect effect : penalties) {
                                this.announce(ordering.creature.applyEffect(effect));
                            }
                        }
                    }
                });
    }

    @Override
    public Collection<Creature> getCreatures() {
        return this.actionPools.keySet();
    }

    @Override
    public Optional<Creature> removeCreature(String name) {
        Optional<Creature> found = this.getCreature(name);
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
    public boolean addCreature(Creature c) {
        if (!c.isInBattle() && !this.hasCreature(c)) {
            if (this.actionPools.putIfAbsent(c, new LinkedBlockingDeque<>(MAX_POOLED_ACTIONS)) == null) {
                c.setInBattle(true);
                c.setSuccessor(this);
                this.battleStats.initialize(this.getCreatures());
                boolean ongoing = this.isBattleOngoing();
                JoinBattleMessage.Builder joinedMessage = JoinBattleMessage.getBuilder().setJoiner(c)
                        .setOngoing(ongoing).setBroacast();// new JoinBattleMessage(c, this.isBattleOngoing(),
                                                           // false);
                if (this.room != null) {
                    this.room.announce(joinedMessage.Build(), c);
                } else {
                    this.announce(joinedMessage.Build(), c);
                }
                c.sendMsg(joinedMessage.setNotBroadcast().Build());
                if (ongoing) {
                    RoundThread thread = this.battleThread.get();
                    if (thread != null) {
                        thread.roundPhaser.register();
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeCreature(Creature c) {
        if (this.actionPools.remove(c) != null) {
            c.setInBattle(false);
            c.setSuccessor(this.successor);
            this.battleStats.remove(c.getName());
            if (!this.checkCompetingFactionsPresent()) {
                endBattle();
            }
            return true;
        }
        return false;
    }

    private boolean checkCompetingFactionsPresent() {
        this.battleLogger.log(Level.FINE, () -> "checking for competing factions");
        Collection<Creature> battlers = this.getCreatures();
        if (battlers == null || battlers.size() <= 1) {
            this.battleLogger.log(Level.FINER, () -> "No or too few battlers");
            return false;
        }
        HashMap<CreatureFaction, Integer> factionCounts = new HashMap<>();
        for (Creature creature : battlers) {
            CreatureFaction thatone = creature.getFaction();
            if (factionCounts.containsKey(thatone)) {
                factionCounts.put(thatone, factionCounts.get(thatone) + 1);
            } else {
                factionCounts.put(thatone, 1);
            }
        }
        this.battleLogger.log(Level.FINER, () -> {
            StringJoiner sj = new StringJoiner(" ").setEmptyValue("No factions found");
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

    public boolean isBattleOngoing() {
        RoundThread thread = this.battleThread.get();
        return thread != null && thread.getIsRunning() && this.checkCompetingFactionsPresent();
    }

    public synchronized RoundThread startBattle(Creature instigator, Collection<Creature> victims) {
        RoundThread curThread = this.battleThread.get();
        if (this.battleThread.get() == null || !curThread.getIsRunning()) {
            this.battleLogger.log(Level.FINER, () -> String.format("%s starts a fight", instigator.getName()));
            this.addCreature(instigator);
            if (victims != null) {
                for (Creature c : victims) {
                    this.addCreature(c);
                }
            }
            StartFightMessage.Builder startMessage = StartFightMessage.getBuilder().setInstigator(instigator)
                    .setBroacast();
            if (this.room != null) {
                this.room.announce(startMessage.Build());
            }
            this.announce(startMessage.setNotBroadcast().Build());
            // if someone started a fight, no need to prompt them for their turn
            RoundThread thread = new RoundThread();
            this.battleLogger.log(Level.INFO, "Starting thread");
            thread.start();
            this.battleThread.set(thread);
        } else {
            this.battleLogger
                    .warning(() -> String.format("%s tried to start an already started fight",
                            instigator.getName()));
        }
        return this.battleThread.get();
    }

    public void endBattle() {
        this.battleLogger.log(Level.INFO, "Ending battle");
        FightOverMessage.Builder foverBuilder = FightOverMessage.getBuilder().setNotBroadcast();
        this.announce(foverBuilder.Build());
        if (this.room != null) {
            this.room.announce(foverBuilder.setBroacast().Build());
        }
        RoundThread thread = this.battleThread.get();
        if (thread != null) {
            thread.killIt();
        }
    }

    @Override
    public boolean onCreatureDeath(Creature creature) {
        boolean removed = this.removeCreature(creature);
        if (this.room != null) {
            removed = this.room.onCreatureDeath(creature) || removed;
        }
        return removed;
    }

    private void clearDead() {
        synchronized (this.actionPools) {
            for (Iterator<Map.Entry<Creature, Deque<IPoolEntry>>> iterator = this.actionPools.entrySet()
                    .iterator(); iterator.hasNext();) {
                Creature creature = iterator.next().getKey();
                if (!creature.isAlive()) {
                    iterator.remove();
                    this.onCreatureDeath(creature);
                }
            }
        }
    }

    public void handleTurnRenegade(Creature turned) {
        if (!CreatureFaction.RENEGADE.equals(turned.getFaction())) {
            turned.setFaction(CreatureFaction.RENEGADE);
            RenegadeAnnouncement.Builder builder = RenegadeAnnouncement.getBuilder(turned);
            turned.sendMsg(builder.setNotBroadcast().Build());
            builder.setBroacast();
            if (this.room != null) {
                room.announce(builder.Build());
            } else {
                this.announce(builder.Build());
            }
        }
    }

    public boolean checkAndHandleTurnRenegade(Creature attacker, Creature target) {
        if (!CreatureFaction.RENEGADE.equals(target.getFaction())
                && !CreatureFaction.RENEGADE.equals(attacker.getFaction())
                && attacker.getFaction() != null
                && attacker.getFaction().allied(target.getFaction())) {
            this.handleTurnRenegade(attacker);
            return true;
        }
        return false;
    }

    /**
     * Calculates a penalty for if player does not respond.
     * Scales to the level of the player.
     * 
     * @param waster
     * @return Set<CreatureEffect>
     */
    private Set<CreatureEffect> calculateWastePenalty(Creature waster) {
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
        builder2.append("BattleManager [participants=").append(this.actionPools.keySet()).append(", room=").append(room)
                .append(", ongoing=").append(this.isBattleOngoing()).append("]");
        return builder2.toString();
    }

    @Override
    public SeeOutMessage produceMessage() {
        SeeOutMessage.Builder seeMessage = SeeOutMessage.getBuilder().setExaminable(this);
        this.getCreatures().stream().filter(creature -> creature != null && creature.isInBattle())
                .forEach(creature -> seeMessage.addSeen("Participants", creature));
        return seeMessage.Build();
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder();
        if (this.isBattleOngoing()) {
            sb.append("The battle is on! ");
            // TODO: round count?
        } else {
            sb.append("There is no fight right now. ");
        }
        return sb.toString();
    }

    @Override
    public void setSuccessor(MessageChainHandler successor) {
        this.successor = successor;
    }

    @Override
    public MessageChainHandler getSuccessor() {
        return this.successor;
    }

    @Override
    public Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx) {
        if (this.cmds == null) {
            this.cmds = this.buildCommands();
        }
        return Collections.unmodifiableMap(this.cmds);
    }

    @Override
    public synchronized void log(Level logLevel, String logMessage) {
        this.battleLogger.log(logLevel, logMessage);
    }

    @Override
    public synchronized void log(Level logLevel, Supplier<String> logMessageSupplier) {
        this.battleLogger.log(logLevel, logMessageSupplier);
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        if (ctx.getBattleManager() == null) {
            ctx.setBattleManager(this);
        }
        return ctx;
    }

    public interface BattleManagerCommandHandler extends Creature.CreatureCommandHandler {
        static final Predicate<CommandContext> defaultBattlePredicate = Creature.CreatureCommandHandler.defaultCreaturePredicate
                .and(ctx -> ctx.getBattleManager() != null);

    }

    public interface PooledBattleManagerCommandHandler extends PooledCommandHandler {
        static final Predicate<CommandContext> defaultTurnPredicate = BattleManagerCommandHandler.defaultBattlePredicate
                .and(ctx -> ctx.getBattleManager().isBattleOngoing());

        @Override
        public default Predicate<CommandContext> getPoolingPredicate() {
            return PooledBattleManagerCommandHandler.defaultTurnPredicate;
        }

        @Override
        default boolean onEmpool(CommandContext ctx, boolean empoolResult) {
            ctx.sendMsg(BattleRoundMessage.getBuilder().setAboutCreature(ctx.getCreature()).setNotBroadcast()
                    .setNeedSubmission(
                            empoolResult ? RoundAcceptance.ACCEPTED
                                    : RoundAcceptance.REJECTED));
            if (empoolResult) {
                RoundThread thread = ctx.getBattleManager().battleThread.get();
                if (thread != null) {
                    thread.roundPhaser.arriveAndDeregister();
                }
            }
            return empoolResult;
        }

        @Override
        default PooledMessageChainHandler<?> getPooledChainHandler(CommandContext ctx) {
            return ctx.getBattleManager();
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
        public Reply handle(CommandContext ctx, Command cmd) {
            ctx.sendMsg(StatsOutMessage.getBuilder().addRecords(BattleManager.this.battleStats.getBattleStatSet())
                    .setNotBroadcast());
            return ctx.handled();
        }

        @Override
        public MessageChainHandler getChainHandler() {
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
                Reply reply = MessageChainHandler.passUpChain(BattleManager.this, ctx, useMessage);
                return reply;
            }
            return ctx.failhandle();
        }

        @Override
        public MessageChainHandler getChainHandler() {
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
        public MessageChainHandler getChainHandler() {
            return BattleManager.this;
        }

    }

    private class SeeHandler implements BattleManagerCommandHandler {
        private static final String helpString = "\"see\" Will give you some information about the battle.\r\n";

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.SEE;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(SeeHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return SeeHandler.defaultBattlePredicate;
        }

        @Override
        public Reply handle(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.SEE && cmd instanceof SeeMessage seeMessage) {
                if (seeMessage.getThing() != null) {
                    return MessageChainHandler.passUpChain(BattleManager.this, ctx, seeMessage);
                }
                ctx.sendMsg(BattleManager.this.produceMessage());
                return ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public MessageChainHandler getChainHandler() {
            return BattleManager.this;
        }

    }

    private class GoHandler implements PooledBattleManagerCommandHandler {
        private final static String helpString = "\"go [direction]\" Try to move in the desired direction and flee the battle, if that direction exists.  Like \"go east\"";
        private final static Predicate<CommandContext> enabledPredicate = BattleManagerCommandHandler.defaultBattlePredicate
                .and(ctx -> {
                    BattleManager bm = ctx.getBattleManager();
                    MessageChainHandler chainHandler = bm.getSuccessor();
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
                FleeMessage.Builder builder = FleeMessage.getBuilder().setRunner(ctx.getCreature()).setRoll(result);
                Reply reply = null;
                if (result.getRoll() >= check) {
                    reply = MessageChainHandler.passUpChain(BattleManager.this, ctx, goMessage);
                }
                if (BattleManager.this.hasCreature(ctx.getCreature())) { // if it is still here, it failed to flee
                    builder.setFled(false);
                    ctx.sendMsg(builder.setFled(false).setNotBroadcast().Build());
                    if (BattleManager.this.room != null) {
                        BattleManager.this.room.announce(builder.setBroacast().Build(), ctx.getCreature());
                    } else {
                        BattleManager.this.announce(builder.setBroacast().Build(), ctx.getCreature());
                    }
                } else {
                    builder.setFled(true).setBroacast();
                    if (BattleManager.this.room != null) {
                        BattleManager.this.room.announce(builder.Build(), ctx.getCreature());
                    } else {
                        BattleManager.this.announce(builder.Build(), ctx.getCreature());
                    }
                }
                return reply != null ? reply.resolve() : ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public MessageChainHandler getChainHandler() {
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
    private List<Creature> collectTargetsFromRoom(Creature attacker, List<String> names) {
        List<Creature> targets = new ArrayList<>();
        BadTargetSelectedMessage.Builder btMessBuilder = BadTargetSelectedMessage.getBuilder().setNotBroadcast();
        if (names == null || names.size() == 0) {
            return targets;
        }
        for (String targetName : names) {
            btMessBuilder.setBadTarget(targetName);
            List<Creature> possTargets = new ArrayList<>(this.room.getCreaturesLike(targetName));
            if (possTargets.size() == 1) {
                Creature targeted = possTargets.get(0);
                if (targeted.equals(attacker)) {
                    attacker.sendMsg(btMessBuilder.setBde(BadTargetOption.SELF).Build());
                    continue; // go to next name
                }
                targets.add(targeted);
            } else {
                btMessBuilder.setPossibleTargets(possTargets);
                if (possTargets.size() == 0) {
                    attacker.sendMsg(btMessBuilder.setBde(BadTargetOption.DNE).Build());
                } else {
                    attacker.sendMsg(btMessBuilder.setBde(BadTargetOption.UNCLEAR).Build());
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
        private final static Predicate<CommandContext> enabledPredicate = Creature.CreatureCommandHandler.defaultCreaturePredicate
                .and(ctx -> {
                    BattleManager bm = ctx.getBattleManager();
                    if (bm == null) {
                        return false;
                    }
                    if (bm.room == null) {
                        return false;
                    }
                    return bm.room.getCreatures().size() > 1 || bm.getCreatures().size() > 1;
                });

        @Override
        public Reply handle(CommandContext ctx, Command cmd) {
            if (cmd == null || !(cmd instanceof AttackMessage)) {
                return ctx.failhandle();
            }
            if (!BattleManager.this.isBattleOngoing()) {
                Creature attacker = ctx.getCreature();
                BattleManager.this.startBattle(attacker,
                        BattleManager.this.collectTargetsFromRoom(attacker, ((AttackMessage) cmd).getTargets()));
                this.onEmpool(ctx, BattleManager.this.empool(ctx, cmd));
                return ctx.handled();
            }
            return this.flushHandle(ctx, cmd);
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

        /**
         * Gets a designated weapon for a creature, either by name, or by default.
         * If a name is provided, but is not found or the item found is not a weapon,
         * then return NULL.
         * 
         * @param attacker   The creature who is attacking
         * @param weaponName The name of a weapon
         * @return a weapon or NULL if the item found is not a weapon or is not found
         */
        private Weapon getDesignatedWeapon(Creature attacker, String weaponName) {
            if (weaponName != null && weaponName.length() > 0) {
                Optional<Item> inventoryItem = attacker.getItem(weaponName);
                NotPossessedMessage.Builder builder = NotPossessedMessage.getBuilder().setNotBroadcast()
                        .setItemName(weaponName).setItemType(Weapon.class.getSimpleName());
                if (inventoryItem.isEmpty()) {
                    attacker.sendMsg(builder.Build());
                    return null;
                } else if (!(inventoryItem.get() instanceof Weapon)) {
                    attacker.sendMsg(builder.setFound(inventoryItem.get()).Build());
                    return null;
                } else {
                    return (Weapon) inventoryItem.get();
                }
            } else {
                return attacker.getWeapon();
            }
        }

        private void applyAttacks(Creature attacker, Weapon weapon, Collection<Creature> targets) {
            for (Creature target : targets) {
                this.log(Level.FINEST,
                        () -> String.format("Applying attack from %s on %s", attacker.getName(), target.getName()));
                BattleManager.this.checkAndHandleTurnRenegade(attacker, target);
                if (!BattleManager.this.hasCreature(target)) {
                    BattleManager.this.addCreature(target);
                }
                BattleManager.this.callReinforcements(attacker, target);
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
                        OutMessage cam = target.applyEffect(effect);
                        BattleManager.this.announce(cam);
                    } else {
                        BattleManager.this.announce(MissMessage.getBuilder().setAttacker(attacker).setTarget(target)
                                .setOffense(attackerResult)
                                .setDefense(targetResult).Build());
                    }
                }

            }
        }

        @Override
        public Reply flushHandle(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == this.getHandleType() && cmd instanceof AttackMessage aMessage) {
                BattleManager.this.battleLogger.log(Level.INFO,
                        ctx.getCreature().getName() + " attempts attacking " + aMessage.getTargets());

                Creature attacker = ctx.getCreature();

                BadTargetSelectedMessage.Builder btMessBuilder = BadTargetSelectedMessage.getBuilder()
                        .setNotBroadcast();

                if (aMessage.getNumTargets() == 0) {
                    ctx.sendMsg(btMessBuilder.setBde(BadTargetOption.NOTARGET).Build());
                    return ctx.handled();
                }

                int numAllowedTargets = 1;
                Vocation attackerVocation = attacker.getVocation();
                if (attackerVocation != null && attackerVocation instanceof MultiAttacker) {
                    numAllowedTargets = ((MultiAttacker) attackerVocation).maxAttackCount(false);
                }

                if (aMessage.getNumTargets() > numAllowedTargets) {
                    String badTarget = aMessage.getTargets().get(numAllowedTargets);
                    ctx.sendMsg(btMessBuilder.setBadTarget(badTarget).setBde(BadTargetOption.TOO_MANY).Build());
                    return ctx.handled();
                }

                List<Creature> targets = BattleManager.this.collectTargetsFromRoom(attacker, aMessage.getTargets());
                if (targets == null || targets.size() == 0) {
                    ctx.sendMsg(btMessBuilder.setBde(BadTargetOption.NOTARGET).Build());
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
        public MessageChainHandler getChainHandler() {
            return BattleManager.this;
        }
    }

    /**
     * Calls for reinforcements for the targetCreature.
     * If the targetCreature is a renegade or has no faction, it cannot call for
     * reinforcements.
     * If the targetCreature *does* call reinforcements, then the attackingCreature
     * gets to
     * call for reinforcements as well.
     * 
     * @param attackingCreature
     * @param targetCreature
     */
    public void callReinforcements(@NotNull Creature attackingCreature, @NotNull Creature targetCreature) {
        ReinforcementsCall.Builder reBuilder = ReinforcementsCall.getBuilder();
        if (targetCreature.getFaction() == null || CreatureFaction.RENEGADE.equals(targetCreature.getFaction())) {
            targetCreature
                    .sendMsg(reBuilder.setNotBroadcast().setCaller(targetCreature).setCallerAddressed(true).Build());
            return;
        }
        if (this.room == null) {
            return;
        }
        Map<CreatureFaction, Set<Creature>> remainingCreatures = this.room.getCreatures().stream()
                .filter(creature -> creature != null && !this.actionPools.keySet().contains(creature))
                .collect(Collectors.groupingBy(creature -> {
                    CreatureFaction faction = creature.getFaction();
                    if (faction == null) {
                        return CreatureFaction.RENEGADE;
                    }
                    return faction;
                }, Collectors.toCollection(() -> new TreeSet<>())));

        if (remainingCreatures == null || remainingCreatures.size() == 0) {
            return;
        }
        Set<Creature> allies = remainingCreatures.get(targetCreature.getFaction());
        if (allies == null || allies.size() == 0) {
            return;
        }
        this.room.announce(reBuilder.setCaller(targetCreature).setBroacast().Build());
        for (Creature c : allies) {
            this.addCreature(c);
        }

        if (attackingCreature.getFaction() == null || CreatureFaction.RENEGADE.equals(attackingCreature.getFaction())) {
            attackingCreature
                    .sendMsg(reBuilder.setCallerAddressed(true).setNotBroadcast().setCaller(attackingCreature).Build());
            return;
        }
        if (!CreatureFaction.NPC.equals(targetCreature.getFaction())) {
            Set<Creature> enemies = remainingCreatures.get(attackingCreature.getFaction());
            if (enemies == null || enemies.size() == 0) {
                return;
            }
            this.room.announce(reBuilder.setBroacast().setCaller(attackingCreature).Build());
            for (Creature c : enemies) {
                this.addCreature(c);
            }
        }
    }

    @Override
    public Collection<ClientMessenger> getClientMessengers() {
        Collection<ClientMessenger> messengers = CreatureContainer.super.getClientMessengers();
        messengers.add(this.battleStats);
        return messengers;
    }

    @Override
    public boolean checkMessageSent(OutMessage outMessage) {
        if (outMessage == null) {
            return true; // yes we "sent" null
        }
        return !this.sentMessage.add(outMessage.getUuid());
    }

}
