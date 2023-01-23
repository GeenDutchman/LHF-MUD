package com.lhf.game.battle;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.lhf.Examinable;
import com.lhf.game.EffectResistance;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.Player;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.item.Item;
import com.lhf.game.item.Weapon;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.map.Room;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.AttackMessage;
import com.lhf.messages.in.SeeMessage;
import com.lhf.messages.out.*;
import com.lhf.messages.out.BadMessage.BadMessageType;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;
import com.lhf.server.interfaces.NotNull;

public class BattleManager implements MessageHandler, Examinable, Runnable {

    private Semaphore turnBarrier;
    private final int turnBarrierWaitCount;
    private final TimeUnit turnBarrierWaitUnit;
    private Thread selfThread;
    private Initiative participants;
    private Room room;
    private AtomicBoolean isHappening;
    private MessageHandler successor;
    private Map<CommandMessage, String> interceptorCmds;
    private Map<CommandMessage, String> cmds;

    public BattleManager(Room room) {
        participants = new FIFOInitiative();
        this.room = room;
        this.successor = this.room;
        this.turnBarrierWaitCount = 1;
        this.turnBarrierWaitUnit = TimeUnit.MINUTES;
        this.init();
    }

    public BattleManager(Room room, int turnWaitCount, TimeUnit turnWaitUnit) {
        participants = new FIFOInitiative();
        this.room = room;
        this.successor = this.room;
        this.turnBarrierWaitCount = turnWaitCount;
        this.turnBarrierWaitUnit = turnWaitUnit;
        this.init();
    }

    public BattleManager(Room room, Initiative initiative, int turnWaitCount, TimeUnit turnWaitUnit) {
        participants = initiative == null ? new FIFOInitiative() : initiative;
        this.room = room;
        this.successor = this.room;
        this.turnBarrierWaitCount = turnWaitCount;
        this.turnBarrierWaitUnit = turnWaitUnit;
        this.init();
    }

    private void init() {
        isHappening = new AtomicBoolean(false);
        this.interceptorCmds = this.buildInterceptorCommands();
        this.cmds = this.buildCommands();
        this.turnBarrier = new Semaphore(0);
        this.selfThread = null;
    }

    public void run() {
        this.participants.start();
        while (this.isBattleOngoing()) {
            this.nextTurn();
            this.startTurn();
            for (int poke = 0; poke <= this.getMaxPokesPerAction(); poke++) {
                try {
                    if (this.turnBarrier.tryAcquire(this.getTurnWaitCount(), this.getTurnWaitUnit())) {
                        break;
                    } else {
                        this.remindCurrent(poke);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    this.endBattle();
                    return;
                }
            }
            this.clearDead();
        }
    }

    private int getTurnWaitCount() {
        return this.turnBarrierWaitCount;
    }

    private TimeUnit getTurnWaitUnit() {
        return this.turnBarrierWaitUnit;
    }

    private int getMaxPokesPerAction() {
        return 2;
    }

    private void remindCurrent(int pokeCount) {
        Creature current = this.getCurrent();
        if (current != null) {
            if (pokeCount < getMaxPokesPerAction()) {
                current.sendMsg(new BattleTurnMessage(current, true, true));
            } else {
                Set<CreatureEffect> penalty = this.calculateWastePenalty(current);
                for (CreatureEffect effect : penalty) {
                    this.sendMessageToAllParticipants(current.applyEffect(effect));
                }
            }
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
        return "Battle";
    }

    public void addCreatureToBattle(Creature c) {
        if (!c.isInBattle() && !this.isCreatureInBattle(c)) {
            if (participants.addCreature(c)) {
                c.setInBattle(true);
                c.setSuccessor(this);
                this.room.sendMessageToAllExcept(new JoinBattleMessage(c, this.isBattleOngoing(), false), c.getName());
                c.sendMsg(new JoinBattleMessage(c, this.isBattleOngoing(), true));
            }
        }
    }

    public void removeCreatureFromBattle(Creature c) {
        if (participants.removeCreature(c)) {
            c.setInBattle(false);
            c.setSuccessor(this.successor);
            if (!this.checkCompetingFactionsPresent()) {
                endBattle();
            }
        }
    }

    private boolean checkCompetingFactionsPresent() {
        Collection<Creature> battlers = this.participants.getParticipants();
        if (battlers == null || battlers.size() <= 1) {
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

    public boolean isCreatureInBattle(Creature c) {
        return participants.hasCreature(c);
    }

    public boolean isBattleOngoing() {
        return isHappening.get();
    }

    public void startBattle(Creature instigator, Collection<Creature> victims) {
        if (this.isHappening.compareAndSet(false, true)) {
            this.addCreatureToBattle(instigator);
            if (victims != null) {
                for (Creature c : victims) {
                    this.addCreatureToBattle(c);
                }
            }
            this.room.sendMessageToAll(new StartFightMessage(instigator, false));
            this.participants.announce(new StartFightMessage(instigator, true));
            // if someone started a fight, no need to prompt them for their turn
            this.selfThread = new Thread(this);
            this.selfThread.run();
        }
    }

    public void endBattle() {
        this.participants.announce(new FightOverMessage(true));
        this.participants.stop();
        this.room.sendMessageToAll(new FightOverMessage(false));
        this.isHappening.set(false);
    }

    private void nextTurn() {
        this.participants.nextTurn();
        startTurn();
    }

    private void startTurn() {
        Creature current = getCurrent();
        if (current != null) {
            promptCreatureToAct(current);
        } else {
            // Bad juju
            Logger logger = Logger.getLogger(BattleManager.class.getPackageName());
            logger.severe("Trying to perform a turn for something that can't do it\r\n");
        }
    }

    public boolean endTurn(Creature ender) {
        if (this.checkTurn(ender)) {
            this.endTurn();
            return true;
        }
        return false;
    }

    private void endTurn() {
        this.turnBarrier.release();
    }

    private void clearDead() {
        List<Creature> dead = new ArrayList<>();
        for (Creature c : this.participants.getParticipants()) {
            if (!c.isAlive()) {
                dead.add(c);
            }
        }
        for (Creature c : dead) {
            removeCreatureFromBattle(c);
            Corpse corpse = c.die();
            room.addItem(corpse);

            for (String i : c.getInventory().getItemList()) {
                Item drop = c.removeItem(i).get();
                room.addItem(drop);
            }

            if (c instanceof Player) {
                Player p = (Player) c;
                room.killPlayer(p);
            } else {
                room.removeCreature(c);
            }
        }
    }

    private void promptCreatureToAct(Creature current) {
        // send message to creature that it is their turn
        this.sendMessageToAllParticipants(new BattleTurnMessage(current, true, false));
        current.sendMsg(new BattleTurnMessage(current, true, true));
    }

    public void handleTurnRenegade(Creature turned) {
        if (!CreatureFaction.RENEGADE.equals(turned.getFaction())) {
            turned.setFaction(CreatureFaction.RENEGADE);
            turned.sendMsg(new RenegadeAnnouncement());
            room.sendMessageToAllExcept(new RenegadeAnnouncement(turned), turned.getName());
        }
    }

    public boolean checkAndHandleTurnRenegade(Creature attacker, Creature target) {
        if (!CreatureFaction.RENEGADE.equals(target.getFaction())
                && !CreatureFaction.RENEGADE.equals(attacker.getFaction())
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
        // if we have a current and you are not it, then you cannot act
        Creature current = this.getCurrent();
        if (current != null && attempter != current) {
            // give out of turn message
            attempter.sendMsg(new BattleTurnMessage(attempter, false, true));
            // even if it's not their turn, make sure they are in it
            this.addCreatureToBattle(attempter);
            return false;
        }
        return true;
    }

    private void applyAttacks(Creature attacker, Weapon weapon, Collection<Creature> targets) {
        for (Creature target : targets) {
            this.checkAndHandleTurnRenegade(attacker, target);
            if (!this.isCreatureInBattle(target)) {
                this.addCreatureToBattle(target);
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
                    sendMessageToAllParticipants(cam);
                } else {
                    sendMessageToAllParticipants(new MissMessage(attacker, target, attackerResult, targetResult));
                }
            }

        }
    }

    private Creature getCurrent() {
        return this.participants.getCurrent();
    }

    public void sendMessageToAllParticipants(OutMessage message) {
        this.participants.announce(message);
    }

    public String toString() {
        return this.produceMessage().toString();
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
    public Map<CommandMessage, String> getCommands() {
        Map<CommandMessage, String> collected = this.cmds;
        if (this.isHappening.get()) {
            collected.putAll(this.interceptorCmds);
        }
        return Collections.unmodifiableMap(collected);
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        if (ctx.getBattleManager() == null) {
            ctx.setBattleManager(this);
        }
        return ctx;
    }

    @Override
    public EnumMap<CommandMessage, String> gatherHelp(CommandContext ctx) {
        EnumMap<CommandMessage, String> gathered = MessageHandler.super.gatherHelp(ctx);
        if (ctx.getCreature() == null) {
            gathered.remove(CommandMessage.ATTACK);
            gathered.remove(CommandMessage.DROP);
            gathered.remove(CommandMessage.INTERACT);
            gathered.remove(CommandMessage.TAKE);
            gathered.remove(CommandMessage.CAST);
            gathered.remove(CommandMessage.USE);
        }
        return gathered;
    }

    @Override
    public boolean handleMessage(CommandContext ctx, Command msg) {
        CommandMessage type = msg.getType();
        Boolean handled = false;
        ctx = this.addSelfToContext(ctx);
        if (ctx.getCreature() == null) {
            ctx.sendMsg(new BadMessage(BadMessageType.CREATURES_ONLY, this.room.gatherHelp(ctx), msg));
            return true;
        }
        if (type != null) {
            if (type == CommandMessage.ATTACK) {
                AttackMessage aMessage = (AttackMessage) msg;
                handled = handleAttack(ctx, aMessage);
            } else if (this.isHappening.get() && ctx.getCreature().isInBattle()
                    && this.interceptorCmds.containsKey(type)) {
                if (type == CommandMessage.SEE) {
                    handled = this.handleSee(ctx, msg);
                } else if (type == CommandMessage.GO) {
                    handled = this.handleGo(ctx, msg);
                } else if (type == CommandMessage.INTERACT) {
                    ctx.sendMsg(new HelpMessage(this.gatherHelp(ctx), type));
                    handled = true;
                } else if (type == CommandMessage.TAKE) {
                    ctx.sendMsg(new HelpMessage(this.gatherHelp(ctx), type));
                    handled = true;
                } else if (type == CommandMessage.PASS) {
                    handled = this.handlePass(ctx, msg);
                } else if (type == CommandMessage.USE) {
                    handled = this.handleUse(ctx, msg);
                }
            }
            if (handled) {
                return handled;
            }
        }
        return MessageHandler.super.handleMessage(ctx, msg);
    }

    private boolean handleUse(CommandContext ctx, Command msg) {
        // TODO: test me!
        if (this.checkTurn(ctx.getCreature())) {
            return false;
        }
        return true;
    }

    private boolean handlePass(CommandContext ctx, Command msg) {
        if (this.checkTurn(ctx.getCreature())) {
            this.endTurn();
        }
        return true;
    }

    private boolean handleSee(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.SEE) {
            SeeMessage seeMessage = (SeeMessage) msg;
            if (seeMessage.getThing() != null) {
                ctx.setBattleManager(this);
                return this.room.handleMessage(ctx, msg);
            }
            ctx.sendMsg(this.produceMessage());
            return true;
        }
        return false;
    }

    private Boolean handleGo(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.GO) {
            Integer check = 10 + this.participants.size();
            MultiRollResult result = ctx.getCreature().check(Attributes.DEX);
            if (result.getRoll() < check) {
                ctx.sendMsg(new FleeMessage(ctx.getCreature(), true, result, false));
                this.room.sendMessageToAllExcept(new FleeMessage(ctx.getCreature(), false, result, false),
                        ctx.getCreature().getName());
                return true;
            }
            this.room.sendMessageToAllExcept(new FleeMessage(ctx.getCreature(), false, result, true),
                    ctx.getCreature().getName());
        }
        return MessageHandler.super.handleMessage(ctx, msg);
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
            if (inventoryItem.isEmpty()) {
                attacker.sendMsg(new NotPossessedMessage(Weapon.class.getSimpleName(), weaponName));
                return null;
            } else if (!(inventoryItem.get() instanceof Weapon)) {
                attacker.sendMsg(
                        new NotPossessedMessage(Weapon.class.getSimpleName(), weaponName, inventoryItem.get()));
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
        if (names == null || names.size() == 0) {
            attacker.sendMsg(new BadTargetSelectedMessage(BadTargetOption.NOTARGET, null));
            return null;
        }
        for (String targetName : names) {
            List<Creature> possTargets = this.room.getCreaturesInRoom(targetName);
            if (possTargets.size() == 1) {
                Creature targeted = possTargets.get(0);
                if (targeted.equals(attacker)) {
                    attacker.sendMsg(new BadTargetSelectedMessage(BadTargetOption.SELF, null));
                    return null;
                }
                targets.add(targeted);
            } else {
                if (possTargets.size() == 0) {
                    attacker.sendMsg(new BadTargetSelectedMessage(BadTargetOption.DNE, targetName, possTargets));
                } else {
                    attacker.sendMsg(
                            new BadTargetSelectedMessage(BadTargetOption.UNCLEAR, targetName, possTargets));
                }
                return null;
            }
        }
        return targets;
    }

    private Boolean handleAttack(CommandContext ctx, AttackMessage aMessage) {
        System.out.println(ctx.getCreature().toString() + " attempts attacking " + aMessage.getTargets());

        Creature attacker = ctx.getCreature();

        if (!this.checkTurn(attacker)) {

            return true;
        }

        if (aMessage.getNumTargets() == 0) {
            ctx.sendMsg(new BadTargetSelectedMessage(BadTargetOption.NOTARGET, null));
            return true;
        }

        int numAllowedTargets = 1;
        if (attacker.getVocation() != null && attacker.getVocation().getName().equals("Fighter")) {
            numAllowedTargets += attacker.getVocation().getLevel() / 5; // TODO: move this to fighter
        }

        if (aMessage.getNumTargets() > numAllowedTargets) {
            String badTarget = aMessage.getTargets().get(numAllowedTargets + 1);
            ctx.sendMsg(new BadTargetSelectedMessage(BadTargetOption.TOO_MANY, badTarget));
            return true;
        }

        List<Creature> targets = this.collectTargetsFromRoom(attacker, aMessage.getTargets());
        if (targets == null || targets.size() == 0) {
            return true;
        }

        Weapon weapon = this.getDesignatedWeapon(attacker, aMessage.getWeapon());
        if (weapon == null) {
            return true;
        }

        if (!this.isBattleOngoing()) {
            this.startBattle(attacker, targets);
        }
        this.applyAttacks(attacker, weapon, targets);
        endTurn();
        return true;
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
        if (targetCreature.getFaction() == null || CreatureFaction.RENEGADE.equals(targetCreature.getFaction())) {
            targetCreature.sendMsg(new ReinforcementsCall(targetCreature, true));
            return;
        }
        int count = this.participants.size();
        this.room.sendMessageToAll(new ReinforcementsCall(targetCreature, false));
        for (Creature c : this.room.getCreaturesInRoom()) {
            if (targetCreature.getFaction().equals(c.getFaction()) && !this.isCreatureInBattle(c)) {
                this.addCreatureToBattle(c);
            }
        }
        if (attackingCreature.getFaction() == null || CreatureFaction.RENEGADE.equals(attackingCreature.getFaction())) {
            attackingCreature.sendMsg(new ReinforcementsCall(attackingCreature, true));
            return;
        }
        if (this.participants.size() > count && !CreatureFaction.NPC.equals(targetCreature.getFaction())) {
            this.room.sendMessageToAll(new ReinforcementsCall(attackingCreature, false));
            for (Creature c : this.room.getCreaturesInRoom()) {
                if (attackingCreature.getFaction().equals(c.getFaction()) && !this.isCreatureInBattle(c)) {
                    this.addCreatureToBattle(c);
                }
            }
        }
    }

}
