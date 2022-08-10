package com.lhf.game.battle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.logging.Logger;

import com.lhf.Examinable;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.Player;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Item;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.item.interfaces.Weapon;
import com.lhf.game.magic.CreatureTargetingSpell;
import com.lhf.game.magic.ISpell;
import com.lhf.game.magic.strategies.CasterVsCreatureStrategy;
import com.lhf.game.map.Room;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.AttackMessage;
import com.lhf.messages.in.SeeMessage;
import com.lhf.messages.out.*;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;

public class BattleManager implements MessageHandler, Examinable {

    private Deque<Creature> participants;
    private Room room;
    private boolean isHappening;
    private MessageHandler successor;
    private Map<CommandMessage, String> interceptorCmds;
    private Map<CommandMessage, String> cmds;

    public BattleManager(Room room) {
        participants = new ArrayDeque<>();
        this.room = room;
        isHappening = false;
        this.successor = this.room;
        this.interceptorCmds = this.buildInterceptorCommands();
        this.cmds = this.buildCommands();
    }

    private Map<CommandMessage, String> buildInterceptorCommands() {
        StringJoiner sj = new StringJoiner(" ");
        Map<CommandMessage, String> cmds = new HashMap<>();
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
        return cmds;
    }

    private Map<CommandMessage, String> buildCommands() {
        StringJoiner sj = new StringJoiner(" ");
        Map<CommandMessage, String> cmds = new HashMap<>();
        sj.add("\"attack [name]\"").add("Attacks a creature").add("\r\n");
        sj.add("\"attack [name] with [weapon]\"").add("Attack the named creature with a weapon that you have.");
        sj.add("In the unlikely event that either the creature or the weapon's name contains 'with', enclose the name in quotation marks.");
        cmds.put(CommandMessage.ATTACK, sj.toString());
        sj = new StringJoiner(" ");
        sj.add("\"pass\"").add("Skips your turn in battle, but you get hurt for doing so!");
        cmds.put(CommandMessage.PASS, sj.toString());
        return cmds;
    }

    @Override
    public String getName() {
        return "Battle";
    }

    public void addCreatureToBattle(Creature c) {
        if (!c.isInBattle() && !this.isCreatureInBattle(c)) {
            participants.addLast(c);
            c.setInBattle(true);
            c.setSuccessor(this);
            this.room.sendMessageToAllExcept(new JoinBattleMessage(c, this.isBattleOngoing(), false), c.getName());
            c.sendMsg(new JoinBattleMessage(c, this.isBattleOngoing(), true));
        }
    }

    public void removeCreatureFromBattle(Creature c) {
        participants.remove(c);
        c.setInBattle(false);
        c.setSuccessor(this.successor);
        if (!this.checkCompetingFactionsPresent()) {
            endBattle();
        }
    }

    private boolean checkCompetingFactionsPresent() {
        if (this.participants.size() <= 1) {
            return false;
        }
        HashMap<CreatureFaction, Integer> factionCounts = new HashMap<>();
        for (Creature creature : participants) {
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
        return participants.contains(c);
    }

    public boolean isBattleOngoing() {
        return isHappening;
    }

    public void startBattle(Creature instigator, Collection<Creature> victims) {
        isHappening = true;
        this.addCreatureToBattle(instigator);
        if (victims != null) {
            for (Creature c : victims) {
                this.addCreatureToBattle(c);
            }
        }
        this.room.sendMessageToAll(new StartFightMessage(instigator, false));
        for (Creature creature : participants) {
            creature.sendMsg(new StartFightMessage(instigator, true));
        }
        // If the player started the fight, then it already has an action
        // about to happen. No need to prompt them for it.
        if (!(instigator instanceof Player)) {
            startTurn();
        }
    }

    public void endBattle() {
        for (Creature creature : participants) {
            creature.sendMsg(new FightOverMessage(true));
            creature.setInBattle(false);
        }
        participants.clear();
        this.room.sendMessageToAll(new FightOverMessage(false));
        isHappening = false;
    }

    private void nextTurn() {
        Creature current = participants.removeFirst();
        participants.addLast(current);
        startTurn();
    }

    private void startTurn() {
        Creature current = getCurrent();
        if (current instanceof Player) {
            // prompt player to do something
            promptCreatureToAct((Player) current);
        } else if (current instanceof BattleAI) {
            Collection<Creature> targets = ((BattleAI) current).selectAttackTargets(participants);
            applyAttacks(current, current.getWeapon(), targets);
            endTurn();
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
        clearDead();
        if (isBattleOngoing()) {
            nextTurn();
        }
    }

    private void clearDead() {
        List<Creature> dead = new ArrayList<>();
        for (Creature c : participants) {
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
        current.sendMsg(new BattleTurnMessage(current, true, false));
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

    private int calculateWastePenalty(Creature waster) {
        int penalty = -1;
        if (waster.getVocation().isPresent()) {
            int wasterLevel = waster.getVocation().get().getLevel();
            penalty += wasterLevel > 0 ? -1 * wasterLevel : wasterLevel;
        }
        return penalty;
    }

    /**
     * If the battle has no participants, it is your turn.
     * Otherwise, if it is not the attemter's turn, then warn them and add them to
     * to fight!
     * 
     * @param attempter tries to see if it is their turn
     * @return true if it is their turn, false otherwise
     */
    public boolean checkTurn(Creature attempter) {
        // if we have a current and you are not it, then you cannot act
        if (this.getCurrent() != null && attempter != this.getCurrent()) {
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
            this.checkAndHandleTurnRenegade(attacker, target)
            if (!this.isCreatureInBattle(target)) {
                this.addCreatureToBattle(target);
                this.callReinforcements(attacker, target);
            }
            Attack a = attacker.attack(weapon);
            if (target.getStats().get(Stats.AC) > a.getToHit().getRoll()) { // misses
                sendMessageToAllParticipants(new MissMessage(attacker, target, a.getToHit(), null));
            } else {
                sendMessageToAllParticipants(target.applyAffects(a));
            }
        }
    }

    private Creature getCurrent() {
        return this.participants.peekFirst();
    }

    public void sendMessageToAllParticipants(OutMessage message) {
        for (Creature c : participants) {
            c.sendMsg(message);
        }
    }

    public String toString() {
        return this.produceMessage().toString();
    }

    @Override
    public SeeOutMessage produceMessage() {
        SeeOutMessage seeMessage = new SeeOutMessage(this, "Current: " + this.getCurrent().getColorTaggedName());
        for (Creature c : participants) {
            seeMessage.addSeen("Participants", c);
        }
        return seeMessage;
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder();
        if (this.isBattleOngoing()) {
            sb.append("The battle is on! ");
        } else {
            sb.append("There is no fight right now. ");
        }
        return sb.toString();
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
        if (this.isHappening) {
            collected.putAll(this.interceptorCmds);
        }
        return collected;
    }

    @Override
    public Boolean handleMessage(CommandContext ctx, Command msg) {
        CommandMessage type = msg.getType();
        Boolean handled = false;
        if (type != null) {
            if (type == CommandMessage.ATTACK) {
                AttackMessage aMessage = (AttackMessage) msg;
                handled = handleAttack(ctx, aMessage);
            } else if (this.isHappening && ctx.getCreature().isInBattle() && this.interceptorCmds.containsKey(type)) {
                if (type == CommandMessage.SEE) {
                    handled = this.handleSee(ctx, msg);
                } else if (type == CommandMessage.GO) {
                    handled = this.handleGo(ctx, msg);
                } else if (type == CommandMessage.INTERACT) {
                    ctx.sendMsg(new HelpMessage(this.gatherHelp(), type));
                    handled = true;
                } else if (type == CommandMessage.TAKE) {
                    ctx.sendMsg(new HelpMessage(this.gatherHelp(), type));
                    handled = true;
                } else if (type == CommandMessage.PASS) {
                    handled = this.handlePass(ctx, msg);
                }
            }
            if (handled) {
                return handled;
            }
        }
        ctx.setBattleManager(this);
        return MessageHandler.super.handleMessage(ctx, msg);
    }

    // TODO: this is perhaps unfair
    private boolean handlePass(CommandContext ctx, Command msg) {
        if (this.checkTurn(ctx.getCreature())) {
            int penalty = this.calculateWastePenalty(ctx.getCreature());
            sendMessageToAllParticipants(new BattleTurnMessage(ctx.getCreature(), true, penalty));
            ctx.getCreature().updateHitpoints(penalty);
            this.endTurn();
        }
        return true;
    }

    private boolean handleSee(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.SEE) {
            SeeMessage seeMessage = (SeeMessage) msg;
            if (seeMessage.getThing() == null) {
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

    private Boolean handleAttack(CommandContext ctx, AttackMessage aMessage) {
        System.out.println(ctx.getCreature().toString() + " attempts attacking " + aMessage.getTargets());

        Creature attacker = ctx.getCreature();

        if (!this.checkTurn(attacker)) {

            return true;
        }

        String weaponName = aMessage.getWeapon();
        Weapon weapon;
        if (weaponName != null && weaponName.length() > 0) {
            Optional<Item> inventoryItem = ctx.getCreature().getItem(weaponName);
            if (inventoryItem.isEmpty()) {
                attacker.sendMsg(new NotPossessedMessage(Weapon.class.getSimpleName(), weaponName));
                return true;
            } else if (!(inventoryItem.get() instanceof Weapon)) {
                attacker.sendMsg(
                        new NotPossessedMessage(Weapon.class.getSimpleName(), weaponName, inventoryItem.get()));
                return true;
            } else {
                weapon = (Weapon) inventoryItem.get();
            }
        } else {
            weapon = attacker.getWeapon();
        }

        int numAllowedTargets = 1;
        if (attacker.getVocation().isPresent() && attacker.getVocation().get().getName().equals("Fighter")) {
            numAllowedTargets += attacker.getVocation().get().getLevel() / 5;
        }

        if (aMessage.getNumTargets() == 0) {
            ctx.sendMsg(new BadTargetSelectedMessage(BadTargetOption.NOTARGET, null));
            return true;
        }

        if (aMessage.getNumTargets() > numAllowedTargets) {
            String badTarget = aMessage.getTargets().get(numAllowedTargets + 1);
            ctx.sendMsg(new BadTargetSelectedMessage(BadTargetOption.TOO_MANY, badTarget));
            return true;
        }

        List<Creature> targets = new ArrayList<>();
        for (String targetName : aMessage.getTargets()) {
            List<Creature> possTargets = this.room.getCreaturesInRoom(targetName);
            if (possTargets.size() == 1) {
                Creature targeted = possTargets.get(0);
                if (targeted.equals(attacker)) {
                    attacker.sendMsg(new BadTargetSelectedMessage(BadTargetOption.SELF, null));
                    return true;
                }
                targets.add(targeted);
            } else {
                if (possTargets.size() == 0) {
                    ctx.sendMsg(new BadTargetSelectedMessage(BadTargetOption.DNE, targetName, possTargets));
                } else {
                    ctx.sendMsg(
                            new BadTargetSelectedMessage(BadTargetOption.UNCLEAR, targetName, possTargets));
                }
                return true;
            }
        }

        if (!this.isBattleOngoing()) {
            this.startBattle(attacker, targets);
        }
        this.applyAttacks(attacker, weapon, targets);
        endTurn();
        return true;
    }

    public void callReinforcements(Creature attackingCreature, Creature targetCreature) {
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
