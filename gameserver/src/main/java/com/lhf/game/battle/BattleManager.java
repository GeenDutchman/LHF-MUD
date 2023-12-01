package com.lhf.game.battle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.CreatureContainerMessageHandler;
import com.lhf.game.EffectResistance;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.item.Item;
import com.lhf.game.item.Weapon;
import com.lhf.game.item.Usable;
import com.lhf.game.map.Area;
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageChainHandler;
import com.lhf.messages.in.AttackMessage;
import com.lhf.messages.in.GoMessage;
import com.lhf.messages.in.PassMessage;
import com.lhf.messages.in.SeeMessage;
import com.lhf.messages.in.UseMessage;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;
import com.lhf.messages.out.BattleTurnMessage;
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

public class BattleManager implements CreatureContainerMessageHandler {
    private final int turnBarrierWaitCount;
    private final TimeUnit turnBarrierWaitUnit;
    private AtomicReference<BattleManagerThread> battleThread;
    private Initiative participants;
    private BattleStats battleStats;
    private Area room;
    private transient MessageChainHandler successor;
    private transient Map<CommandMessage, CommandHandler> cmds;
    private Logger battleLogger;
    private transient Set<UUID> sentMessage;

    public static class Builder {
        private int waitCount;
        private TimeUnit waitUnit;
        private Initiative.Builder initiativeBuilder;

        public static Builder getInstance() {
            return new Builder();
        }

        private Builder() {
            this.waitCount = 1;
            this.waitUnit = TimeUnit.MINUTES;
            this.initiativeBuilder = FIFOInitiative.Builder.getInstance();
        }

        public Builder setWaitCount(int count) {
            this.waitCount = count <= 0 ? 1 : count;
            return this;
        }

        public Builder setUnit(TimeUnit unit) {
            this.waitUnit = unit != null ? unit : TimeUnit.MINUTES;
            return this;
        }

        public Builder setInitiativeBuilder(Initiative.Builder builder) {
            this.initiativeBuilder = builder != null ? builder : FIFOInitiative.Builder.getInstance();
            return this;
        }

        public BattleManager Build(Area room) {
            return new BattleManager(room, this);
        }
    }

    protected class BattleManagerThread extends Thread {
        protected AtomicBoolean isRunning;
        protected Logger threadLogger;
        private Semaphore turnBarrier;

        protected BattleManagerThread() {
            this.isRunning = new AtomicBoolean(false);
            this.threadLogger = Logger.getLogger(this.getClass().getName());
            this.turnBarrier = new Semaphore(0);
        }

        @Override
        public void run() {
            this.threadLogger.log(Level.INFO, "Running");
            BattleManager.this.participants.start();
            this.isRunning.set(true);
            while (this.isRunning.get()) {
                Creature current = BattleManager.this.startTurn(threadLogger);
                for (int poke = 0; poke <= BattleManager.this.getMaxPokesPerAction(); poke++) {
                    try {
                        threadLogger.log(Level.FINEST, String.format("Waiting %d at turnBarrier for %s for %d %s", poke,
                                current.getName(), BattleManager.this.getTurnWaitCount(),
                                BattleManager.this.getTurnWaitUnit()));
                        if (this.turnBarrier.tryAcquire(BattleManager.this.getTurnWaitCount(),
                                BattleManager.this.getTurnWaitUnit())) {
                            threadLogger.log(Level.FINEST, "Barrier passed");
                            break;
                        } else {
                            threadLogger.log(Level.WARNING,
                                    String.format("Failed to acquire (%d) barrier for %s", poke, current.getName()));
                            BattleManager.this.remindCurrent(threadLogger, poke, current);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        BattleManager.this.endBattle();
                        this.threadLogger.exiting(this.getClass().getName(), "run");
                        return;
                    }
                }
                BattleManager.this.clearDead();
                if (!BattleManager.this.checkCompetingFactionsPresent()) {
                    this.threadLogger.log(Level.INFO, () -> String.format("No compteting factions found"));
                    this.isRunning.set(false);
                    threadLogger.exiting(this.getClass().getName(), "run()");
                    return;
                }
                BattleManager.this.nextTurn(threadLogger);
            }
            threadLogger.exiting(this.getClass().getName(), "run()");
        }

        public boolean endTurn(Creature ender) {
            if (ender == null) {
                this.threadLogger.log(Level.WARNING, () -> "A null creature cannot end their turn!");
                return false;
            }
            if (BattleManager.this.checkTurn(ender)) {
                this.threadLogger.log(Level.FINEST, () -> String.format("%s has ended their turn", ender.getName()));
                this.turnBarrier.release();
                return true;
            } else {
                this.threadLogger.log(Level.WARNING,
                        () -> String.format("%s tried to end their turn, but it wasn't theirs to end",
                                ender != null ? ender.getName() : "Someone unknown"));
                return false;
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
        this.participants = builder.initiativeBuilder.Build();
        this.battleStats = new BattleStats().initialize(this.participants.getCreatures());
        this.room = room;
        this.successor = this.room;
        this.turnBarrierWaitCount = builder.waitCount;
        this.turnBarrierWaitUnit = builder.waitUnit;
        this.sentMessage = new TreeSet<>();
        this.init();
    }

    private void init() {
        this.cmds = this.buildCommands();
        this.battleThread = new AtomicReference<BattleManager.BattleManagerThread>(null);
        this.battleLogger = Logger.getLogger(this.getClass().getName());
    }

    protected int getTurnWaitCount() {
        return this.turnBarrierWaitCount;
    }

    protected TimeUnit getTurnWaitUnit() {
        return this.turnBarrierWaitUnit;
    }

    protected int getMaxPokesPerAction() {
        return 2;
    }

    private void remindCurrent(Logger logger, int pokeCount, Creature current) {
        if (current != null) {
            if (pokeCount < getMaxPokesPerAction()) {
                logger.log(Level.FINER, () -> String.format("Poking %s", current.getName()));
                current.sendMsg(BattleTurnMessage.getBuilder().setYesTurn(true)
                        .fromInitiative(participants)
                        .Build());
            } else {
                logger.log(Level.WARNING, () -> String.format("Last poke for %s", current.getName()));
                Set<CreatureEffect> penalty = this.calculateWastePenalty(current);
                for (CreatureEffect effect : penalty) {
                    this.announce(current.applyEffect(effect));
                }
            }
        } else {
            logger.log(Level.SEVERE, "No current creature!");
        }
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
    public Collection<Creature> getCreatures() {
        return this.participants.getCreatures();
    }

    @Override
    public Optional<Creature> removeCreature(String name) {
        Optional<Creature> found = this.participants.getCreature(name);
        if (found.isPresent()) {
            this.removeCreature(found.get());
        }
        return found;
    }

    @Override
    public Optional<Player> removePlayer(String name) {
        Optional<Player> found = this.participants.getPlayer(name);
        if (found.isPresent()) {
            this.removeCreature(found.get());
        }
        return found;
    }

    @Override
    public Optional<Player> removePlayer(UserID id) {
        Optional<Player> found = this.participants.getPlayer(id);
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
            if (participants.addCreature(c)) {
                c.setInBattle(true);
                c.setSuccessor(this);
                this.battleStats.initialize(this.getCreatures());
                JoinBattleMessage.Builder joinedMessage = JoinBattleMessage.getBuilder().setJoiner(c)
                        .setOngoing(isBattleOngoing()).setBroacast();// new JoinBattleMessage(c, this.isBattleOngoing(),
                                                                     // false);
                if (this.room != null) {
                    this.room.announce(joinedMessage.Build(), c);
                } else {
                    this.announce(joinedMessage.Build(), c);
                }
                c.sendMsg(joinedMessage.setNotBroadcast().Build());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeCreature(Creature c) {
        if (participants.removeCreature(c)) {
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
        Collection<Creature> battlers = this.participants.getCreatures();
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
        BattleManagerThread thread = this.battleThread.get();
        return thread != null && thread.getIsRunning() && this.checkCompetingFactionsPresent();
    }

    public synchronized BattleManagerThread startBattle(Creature instigator, Collection<Creature> victims) {
        BattleManagerThread curThread = this.battleThread.get();
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
            this.participants.announce(startMessage.setNotBroadcast().Build());
            // if someone started a fight, no need to prompt them for their turn
            BattleManagerThread thread = new BattleManagerThread();
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
        this.participants.announce(foverBuilder.Build());
        this.participants.stop();
        if (this.room != null) {
            this.room.announce(foverBuilder.setBroacast().Build());
        }
        BattleManagerThread thread = this.battleThread.get();
        if (thread != null) {
            thread.killIt();
        }
    }

    private Creature nextTurn(Logger logger) {
        logger.log(Level.FINE, "Cycling next turn");
        return this.participants.nextTurn();
    }

    private Creature startTurn(Logger logger) {
        Creature current = getCurrent();
        if (current != null) {
            logger.log(Level.FINEST, () -> String.format("Starting turn for %s", current.getName()));
            promptCreatureToAct(current);
        } else {
            // Bad juju
            logger.log(Level.SEVERE, "Trying to perform a turn for something that can't do it\r\n");
        }
        return current;
    }

    public boolean endTurn(Creature ender) {
        if (ender == null) {
            this.battleLogger.log(Level.WARNING, () -> "A null creature cannot end their turn!");
            return false;
        }
        BattleManagerThread thread = this.battleThread.get();
        if (thread != null) {
            return thread.endTurn(ender);
        }
        this.battleLogger.log(Level.WARNING, () -> "There is no current battle");
        return false;
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
        List<Creature> dead = new ArrayList<>();
        for (Creature c : this.participants.getCreatures()) {
            if (!c.isAlive()) {
                dead.add(c);
            }
        }
        for (Creature c : dead) {
            this.onCreatureDeath(c);
        }
    }

    private void promptCreatureToAct(Creature current) {
        // send message to creature that it is their turn
        BattleTurnMessage.Builder builder = BattleTurnMessage.getBuilder().fromInitiative(participants)
                .setCurrentCreature(current)
                .setYesTurn(true);
        this.announce(builder.setBroacast().Build());
        current.sendMsg(builder.setNotBroadcast().Build());
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

    /**
     * If the battle has no participants, it is your turn.
     * Otherwise, if it is not the attempter's turn, then warn them and add them to
     * to fight!
     * 
     * @param attempter tries to see if it is their turn
     * @return true if it is their turn, false otherwise
     */
    public boolean checkTurn(Creature attempter) {
        // If you don't exist, you don't have a turn
        if (attempter == null) {
            return false;
        }
        // if we have a current and you are not it, then you cannot act
        Creature current = this.getCurrent();
        if (current != null && attempter != current) {
            // give out of turn message
            attempter.sendMsg(BattleTurnMessage.getBuilder().setYesTurn(false).fromInitiative(participants).Build());
            // even if it's not their turn, make sure they are in it
            this.addCreature(attempter);
            return false;
        }
        this.battleLogger.log(Level.FINE, () -> String.format("Current is NULL, so %s can go!", attempter.getName()));
        return true;
    }

    private Creature getCurrent() {
        Creature current = this.participants.getCurrent();
        this.battleLogger.log(Level.FINEST,
                () -> String.format("Getting current creature (%s)", current != null ? current.getName() : "NULL"));
        return current;
    }

    @Override
    public String toString() {
        StringBuilder builder2 = new StringBuilder();
        builder2.append("BattleManager [participants=").append(participants).append(", room=").append(room)
                .append(", ongoing=").append(this.isBattleOngoing()).append("]");
        return builder2.toString();
    }

    @Override
    public SeeOutMessage produceMessage() {
        return this.participants.produceMessage();
    }

    @Override
    public String printDescription() {
        return this.participants.printDescription();
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
    public CommandContext addSelfToContext(CommandContext ctx) {
        if (ctx.getBattleManager() == null) {
            ctx.setBattleManager(this);
        }
        return ctx;
    }

    public interface BattleManagerCommandHandler extends Creature.CreatureCommandHandler {
        static final Predicate<CommandContext> defaultBattlePredicate = Creature.CreatureCommandHandler.defaultCreaturePredicate
                .and(ctx -> ctx.getBattleManager() != null && ctx.getCreature().isInBattle());
        static final Predicate<CommandContext> defaultTurnPredicate = BattleManagerCommandHandler.defaultBattlePredicate
                .and(ctx -> ctx.getBattleManager().checkTurn(ctx.getCreature()));

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

    private class UseHandler implements BattleManagerCommandHandler {
        private final static Predicate<CommandContext> enabledPredicate = UseHandler.defaultTurnPredicate.and(
                ctx -> ctx.getCreature().getItems().stream().anyMatch(item -> item != null && item instanceof Usable));
        private static String helpString;

        static {
            StringJoiner sj = new StringJoiner(" ");
            sj.add("\"use [itemname]\"").add("Uses an item that you have on yourself, if applicable.")
                    .add("Like \"use potion\"").add("\r\n");
            sj.add("\"use [itemname] on [otherthing]\"")
                    .add("Uses an item that you have on something or someone else, if applicable.")
                    .add("Like \"use potion on Bob\"");
            UseHandler.helpString = sj.toString();
        }

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
        public Reply handle(CommandContext ctx, Command cmd) {
            // TODO: #127 test me!
            if (cmd != null && cmd.getType() == this.getHandleType() && cmd instanceof UseMessage useMessage) {
                Reply reply = MessageChainHandler.passUpChain(BattleManager.this, ctx, useMessage);
                if (reply.isHandled()) {
                    BattleManager.this.endTurn(ctx.getCreature());
                }
                return reply;
            }
            return ctx.failhandle();
        }

        @Override
        public MessageChainHandler getChainHandler() {
            return BattleManager.this;
        }

    }

    private class PassHandler implements BattleManagerCommandHandler {

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
        public Reply handle(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == this.getHandleType() && cmd instanceof PassMessage passMessage) {
                BattleManager.this.endTurn(ctx.getCreature());
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

    private class GoHandler implements BattleManagerCommandHandler {
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
        public Reply handle(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.GO && cmd instanceof GoMessage goMessage) {
                Integer check = 10 + BattleManager.this.participants.size();
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
     * Returns null if there was a problem collecting targets.
     * 
     * @param attacker Creature who selected the targets
     * @param names    names of the targets
     * @return null if there was a problem, otherwise List<Creature> with size >= 1
     */
    private List<Creature> collectTargetsFromRoom(Creature attacker, List<String> names) {
        List<Creature> targets = new ArrayList<>();
        BadTargetSelectedMessage.Builder btMessBuilder = BadTargetSelectedMessage.getBuilder().setNotBroadcast();
        if (names == null || names.size() == 0) {
            attacker.sendMsg(btMessBuilder.setBde(BadTargetOption.NOTARGET).Build());
            return null;
        }
        for (String targetName : names) {
            btMessBuilder.setBadTarget(targetName);
            List<Creature> possTargets = new ArrayList<>(this.room.getCreaturesLike(targetName));
            if (possTargets.size() == 1) {
                Creature targeted = possTargets.get(0);
                if (targeted.equals(attacker)) {
                    attacker.sendMsg(btMessBuilder.setBde(BadTargetOption.SELF).Build());
                    return null;
                }
                targets.add(targeted);
            } else {
                btMessBuilder.setPossibleTargets(possTargets);
                if (possTargets.size() == 0) {
                    attacker.sendMsg(btMessBuilder.setBde(BadTargetOption.DNE).Build());
                } else {
                    attacker.sendMsg(btMessBuilder.setBde(BadTargetOption.UNCLEAR).Build());
                }
                return null;
            }
        }
        return targets;
    }

    private class AttackHandler implements BattleManagerCommandHandler {
        private final static String helpString = new StringJoiner(" ")
                .add("\"attack [name]\"").add("Attacks a creature").add("\r\n")
                .add("\"attack [name] with [weapon]\"").add("Attack the named creature with a weapon that you have.")
                .add("In the unlikely event that either the creature or the weapon's name contains 'with', enclose the name in quotation marks.")
                .toString();
        private final static Predicate<CommandContext> enabledPredicate = AttackHandler.defaultCreaturePredicate
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
                BattleManager.this.checkAndHandleTurnRenegade(attacker, target);
                if (!BattleManager.this.hasCreature(target)) {
                    BattleManager.this.addCreature(target);
                    BattleManager.this.callReinforcements(attacker, target);
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
        public Reply handle(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == this.getHandleType() && cmd instanceof AttackMessage aMessage) {
                BattleManager.this.battleLogger.log(Level.INFO,
                        ctx.getCreature().getName() + " attempts attacking " + aMessage.getTargets());

                Creature attacker = ctx.getCreature();

                if (!BattleManager.this.checkTurn(attacker)) {
                    return ctx.handled();
                }

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
                    return ctx.handled();
                }

                if (!BattleManager.this.isBattleOngoing()) {
                    BattleManager.this.startBattle(attacker, targets);
                }
                this.applyAttacks(attacker, weapon, targets);
                BattleManager.this.endTurn(attacker);
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
        int count = this.participants.size();
        this.room.announce(reBuilder.setCaller(targetCreature).setBroacast().Build());
        for (Creature c : this.room.getCreatures()) {
            if (targetCreature.getFaction().equals(c.getFaction()) && !this.hasCreature(c)) {
                this.addCreature(c);
            }
        }
        if (attackingCreature.getFaction() == null || CreatureFaction.RENEGADE.equals(attackingCreature.getFaction())) {
            attackingCreature
                    .sendMsg(reBuilder.setCallerAddressed(true).setNotBroadcast().setCaller(attackingCreature).Build());
            return;
        }
        if (this.participants.size() > count && !CreatureFaction.NPC.equals(targetCreature.getFaction())) {
            this.room.announce(reBuilder.setBroacast().setCaller(attackingCreature).Build());
            for (Creature c : this.room.getCreatures()) {
                if (attackingCreature.getFaction().equals(c.getFaction()) && !this.hasCreature(c)) {
                    this.addCreature(c);
                }
            }
        }
    }

    @Override
    public Collection<ClientMessenger> getClientMessengers() {
        Collection<ClientMessenger> messengers = CreatureContainerMessageHandler.super.getClientMessengers();
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
