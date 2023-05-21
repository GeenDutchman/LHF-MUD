package com.lhf.game.battle;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.CreatureContainerMessageHandler;
import com.lhf.game.EffectResistance;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.Player;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.item.Item;
import com.lhf.game.item.Weapon;
import com.lhf.game.map.Area;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.AttackMessage;
import com.lhf.messages.in.SeeMessage;
import com.lhf.messages.out.*;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;
import com.lhf.server.client.user.UserID;
import com.lhf.server.interfaces.NotNull;

public class BattleManager implements CreatureContainerMessageHandler {
    private final int turnBarrierWaitCount;
    private final TimeUnit turnBarrierWaitUnit;
    private AtomicReference<BattleManagerThread> battleThread;
    private Initiative participants;
    private Area room;
    private transient MessageHandler successor;
    private Map<CommandMessage, String> interceptorCmds;
    private Map<CommandMessage, String> cmds;
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
        this.room = room;
        this.successor = this.room;
        this.turnBarrierWaitCount = builder.waitCount;
        this.turnBarrierWaitUnit = builder.waitUnit;
        this.sentMessage = new TreeSet<>();
        this.init();
    }

    private void init() {
        this.interceptorCmds = this.buildInterceptorCommands();
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

    private Map<CommandMessage, String> buildInterceptorCommands() {
        StringJoiner sj = new StringJoiner(" ");
        Map<CommandMessage, String> cmds = new EnumMap<>(CommandMessage.class);
        sj = new StringJoiner(" "); // clear
        sj.add("\"see\"").add("Will give you some information about the battle.\r\n");
        cmds.put(CommandMessage.SEE, sj.toString());
        sj = new StringJoiner(" ");
        sj.add("\"go [direction]\"").add(
                "Try to move in the desired direction and flee the battle, if that direction exists.  Like \"go east\"");
        cmds.put(CommandMessage.GO, sj.toString());
        sj = new StringJoiner(" ");
        sj.add("\"interact [item]\"").add("You cannot interact with things right now.");
        cmds.put(CommandMessage.INTERACT, sj.toString());
        sj = new StringJoiner(" ");
        sj.add("\"take [item]\"").add("You can't mess with stuff on the floor right now.");
        cmds.put(CommandMessage.TAKE, sj.toString());
        sj = new StringJoiner(" ");
        sj.add("\"pass\"").add("Skips your turn in battle!");
        cmds.put(CommandMessage.PASS, sj.toString());
        sj = new StringJoiner(" ");
        sj.add("\"use [itemname]\"").add("Uses an item that you have on yourself, if applicable.")
                .add("Like \"use potion\"").add("\r\n");
        sj.add("\"use [itemname] on [otherthing]\"")
                .add("Uses an item that you have on something or someone else, if applicable.")
                .add("Like \"use potion on Bob\"");
        cmds.put(CommandMessage.USE, sj.toString());
        return cmds;
    }

    private Map<CommandMessage, String> buildCommands() {
        StringJoiner sj = new StringJoiner(" ");
        Map<CommandMessage, String> cmds = new EnumMap<>(CommandMessage.class);
        sj.add("\"attack [name]\"").add("Attacks a creature").add("\r\n");
        sj.add("\"attack [name] with [weapon]\"").add("Attack the named creature with a weapon that you have.");
        sj.add("In the unlikely event that either the creature or the weapon's name contains 'with', enclose the name in quotation marks.");
        cmds.put(CommandMessage.ATTACK, sj.toString());
        sj = new StringJoiner(" ");
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
                && attacker.getFaction().equals(target.getFaction())) {
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

    private void applyAttacks(Creature attacker, Weapon weapon, Collection<Creature> targets) {
        for (Creature target : targets) {
            this.checkAndHandleTurnRenegade(attacker, target);
            if (!this.hasCreature(target)) {
                this.addCreature(target);
                this.callReinforcements(attacker, target);
            }
            Attack a = attacker.attack(weapon);

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
                    announce(cam);
                } else {
                    announce(MissMessage.getBuilder().setAttacker(attacker).setTarget(target).setOffense(attackerResult)
                            .setDefense(targetResult).Build());
                }
            }

        }
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
    public void setSuccessor(MessageHandler successor) {
        this.successor = successor;
    }

    @Override
    public MessageHandler getSuccessor() {
        return this.successor;
    }

    @Override
    public Map<CommandMessage, String> getCommands(CommandContext ctx) {
        Map<CommandMessage, String> collected = new EnumMap<>(this.cmds);
        if (this.isBattleOngoing()) {
            collected.putAll(this.interceptorCmds);
        }
        if (ctx.getCreature() == null) {
            collected.remove(CommandMessage.ATTACK);
            collected.remove(CommandMessage.DROP);
            collected.remove(CommandMessage.INTERACT);
            collected.remove(CommandMessage.TAKE);
            collected.remove(CommandMessage.CAST);
            collected.remove(CommandMessage.USE);
        } else if (!this.isBattleOngoing() || !ctx.getCreature().isInBattle()) {
            collected.remove(CommandMessage.INTERACT);
            collected.remove(CommandMessage.PASS);
            collected.remove(CommandMessage.GO);
            collected.remove(CommandMessage.TAKE);
            collected.remove(CommandMessage.USE);
        }
        return ctx.addHelps(Collections.unmodifiableMap(collected));
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        if (ctx.getBattleManager() == null) {
            ctx.setBattleManager(this);
        }
        return ctx;
    }

    @Override
    public CommandContext.Reply handleMessage(CommandContext ctx, Command msg) {
        CommandMessage type = msg.getType();
        CommandContext.Reply handled = ctx.failhandle();
        ctx = this.addSelfToContext(ctx);
        if (type != null && this.getCommands(ctx).containsKey(type)) {
            if (type == CommandMessage.ATTACK) {
                AttackMessage aMessage = (AttackMessage) msg;
                handled = handleAttack(ctx, aMessage);
            } else if (this.isBattleOngoing() && ctx.getCreature().isInBattle()
                    && this.interceptorCmds.containsKey(type)) {
                if (type == CommandMessage.SEE) {
                    handled = this.handleSee(ctx, msg);
                } else if (type == CommandMessage.GO) {
                    handled = this.handleGo(ctx, msg);
                } else if (type == CommandMessage.PASS) {
                    handled = this.handlePass(ctx, msg);
                } else if (type == CommandMessage.USE) {
                    handled = this.handleUse(ctx, msg);
                }
            }
            if (handled.isHandled()) {
                return handled;
            }
        }
        return CreatureContainerMessageHandler.super.handleMessage(ctx, msg);
    }

    private CommandContext.Reply handleUse(CommandContext ctx, Command msg) {
        // TODO: test me!
        if (this.checkTurn(ctx.getCreature())) {
            CommandContext.Reply reply = CreatureContainerMessageHandler.super.handleMessage(ctx, msg);
            if (reply.isHandled()) {
                this.endTurn(ctx.getCreature());
            }
            return reply;
        }
        return ctx.handled();
    }

    private CommandContext.Reply handlePass(CommandContext ctx, Command msg) {
        Creature creature = ctx.getCreature();
        if (this.checkTurn(creature)) {
            this.endTurn(creature);
        }
        return ctx.handled();
    }

    private CommandContext.Reply handleSee(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.SEE) {
            SeeMessage seeMessage = (SeeMessage) msg;
            if (seeMessage.getThing() != null) {
                ctx.setBattleManager(this);
                if (this.room != null) {
                    return this.room.handleMessage(ctx, msg);
                }
                return ctx.failhandle();
            }
            ctx.sendMsg(this.produceMessage());
            return ctx.handled();
        }
        return ctx.failhandle();
    }

    private CommandContext.Reply handleGo(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.GO) {
            Integer check = 10 + this.participants.size();
            MultiRollResult result = ctx.getCreature().check(Attributes.DEX);
            FleeMessage.Builder builder = FleeMessage.getBuilder().setRunner(ctx.getCreature()).setRoll(result);
            if (result.getRoll() < check) {
                ctx.sendMsg(builder.setFled(false).setNotBroadcast().Build());
                if (this.room != null) {
                    this.room.announce(builder.setBroacast().Build(), ctx.getCreature());
                } else {
                    this.announce(builder.setBroacast().Build(), ctx.getCreature());
                }
                return ctx.handled();
            }
            builder.setFled(true).setBroacast();
            if (this.room != null) {
                this.room.announce(builder.Build(), ctx.getCreature());
            } else {
                this.announce(builder.Build(), ctx.getCreature());
            }
            return CreatureContainerMessageHandler.super.handleMessage(ctx, msg);
        }
        return ctx.failhandle();
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

    private CommandContext.Reply handleAttack(CommandContext ctx, AttackMessage aMessage) {
        this.battleLogger.log(Level.INFO, ctx.getCreature().getName() + " attempts attacking " + aMessage.getTargets());

        Creature attacker = ctx.getCreature();

        if (!this.checkTurn(attacker)) {

            return ctx.handled();
        }

        BadTargetSelectedMessage.Builder btMessBuilder = BadTargetSelectedMessage.getBuilder().setNotBroadcast();

        if (aMessage.getNumTargets() == 0) {
            ctx.sendMsg(btMessBuilder.setBde(BadTargetOption.NOTARGET).Build());
            return ctx.handled();
        }

        int numAllowedTargets = 1;
        if (attacker.getVocation() != null && attacker.getVocation().getName().equals("Fighter")) {
            numAllowedTargets += attacker.getVocation().getLevel() / 5; // TODO: move this to fighter
        }

        if (aMessage.getNumTargets() > numAllowedTargets) {
            String badTarget = aMessage.getTargets().get(numAllowedTargets + 1);
            ctx.sendMsg(btMessBuilder.setBadTarget(badTarget).setBde(BadTargetOption.TOO_MANY).Build());
            return ctx.handled();
        }

        List<Creature> targets = this.collectTargetsFromRoom(attacker, aMessage.getTargets());
        if (targets == null || targets.size() == 0) {
            ctx.sendMsg(btMessBuilder.setBde(BadTargetOption.NOTARGET).Build());
            return ctx.handled();
        }

        Weapon weapon = this.getDesignatedWeapon(attacker, aMessage.getWeapon());
        if (weapon == null) {
            return ctx.handled();
        }

        if (!this.isBattleOngoing()) {
            this.startBattle(attacker, targets);
        }
        this.applyAttacks(attacker, weapon, targets);
        endTurn(attacker);
        return ctx.handled();
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
    public boolean checkMessageSent(OutMessage outMessage) {
        if (outMessage == null) {
            return true; // yes we "sent" null
        }
        return !this.sentMessage.add(outMessage.getUuid());
    }

}
