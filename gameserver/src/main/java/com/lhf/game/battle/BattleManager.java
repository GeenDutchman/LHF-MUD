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
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.dice.DiceD4;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Item;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.item.interfaces.Weapon;
import com.lhf.game.magic.interfaces.CreatureAffector;
import com.lhf.game.magic.interfaces.DamageSpell;
import com.lhf.game.magic.strategies.CasterVsCreatureStrategy;
import com.lhf.game.map.Room;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.AttackMessage;
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
        return cmds;
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

    public void startBattle(Creature instigator) {
        isHappening = true;
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
                Item drop = c.dropItem(i).get();
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

    private void handleTurnRenegade(Creature turned) {
        if (!CreatureFaction.RENEGADE.equals(turned.getFaction())) {
            turned.setFaction(CreatureFaction.RENEGADE);
            turned.sendMsg(new RenegadeAnnouncement());
            room.sendMessageToAllExcept(new RenegadeAnnouncement(turned), turned.getName());
        }
    }

    public void takeAction(Creature attacker, BattleAction action) {

        if (this.getCurrent() != null && attacker != this.getCurrent()) {
            // give out of turn message
            attacker.sendMsg(new BattleTurnMessage(attacker, false, true));
            return;
        }

        if (!this.isBattleOngoing()) {
            this.startBattle(attacker);
        }

        if (action instanceof AttackAction) {
            AttackAction attackAction = (AttackAction) action;

            if (!attackAction.hasTargets()) {
                attacker.sendMsg(new BadTargetSelectedMessage(BadTargetOption.NOTARGET, null));
                return;
            }
            List<Creature> targets = attackAction.getTargets();
            if (targets.contains(attacker)) {
                attacker.sendMsg(new BadTargetSelectedMessage(BadTargetOption.SELF, null));
                return;
            }
            this.addCreatureToBattle(attacker);
            for (Creature targeted : targets) {
                if (!CreatureFaction.RENEGADE.equals(targeted.getFaction())
                        && !CreatureFaction.RENEGADE.equals(attacker.getFaction())
                        && attacker.getFaction().equals(targeted.getFaction())) {
                    this.handleTurnRenegade(attacker);
                }
                if (!this.isCreatureInBattle(targeted)) {
                    this.addCreatureToBattle(targeted);
                    this.callReinforcements(attacker, targeted);
                }
            }

            applyAttacks(attacker, attackAction.getWeapon(), targets);

        } else if (action instanceof CreatureAffector) {
            CreatureAffector spell = (CreatureAffector) action;
            if (!spell.hasTargets()) {
                attacker.sendMsg(new BadTargetSelectedMessage(BadTargetOption.NOTARGET, null));
                return;
            }
            List<Creature> targets = spell.getTargets();
            for (Creature c : targets) {
                if (!isCreatureInBattle(c)) {
                    // invalid target in list
                    attacker.sendMsg(new BadTargetSelectedMessage(BadTargetOption.DNE, c.getName()));
                    return;
                }
                if (spell instanceof DamageSpell && c.getFaction() != CreatureFaction.RENEGADE
                        && attacker.getFaction().equals(c.getFaction())) {
                    this.handleTurnRenegade(attacker);
                }
            }
            applySpell((Creature) spell.getCaster(), (CreatureAffector) spell, targets);
        } else {
            sendMessageToAllParticipants(new BattleTurnMessage(attacker, true));
        }
        endTurn();
    }

    private void applySpell(Creature attacker, CreatureAffector spell, Collection<Creature> targets) {
        sendMessageToAllParticipants(new GameMessage(spell.Cast()));
        Optional<CasterVsCreatureStrategy> strategy = spell.getStrategy();
        for (Creature target : targets) {
            if (strategy.isPresent()) {
                CasterVsCreatureStrategy strat = strategy.get();
                RollResult casterResult = strat.getCasterEffort();
                RollResult targetResult = strat.getTargetEffort(target);
                if (casterResult.getTotal() <= targetResult.getTotal()) {
                    sendMessageToAllParticipants(new GameMessage(
                            attacker.getColorTaggedName() + " missed (" + casterResult.getColorTaggedName() + ") "
                                    + target.getColorTaggedName() + " (" + targetResult.getColorTaggedName() + ")."));
                    continue;
                }
            }
            // hack it

            sendMessageToAllParticipants(new GameMessage(target.applySpell(spell)));
        }
    }

    private void applyAttacks(Creature attacker, Weapon weapon, Collection<Creature> targets) {
        for (Creature target : targets) {
            Attack a = attacker.attack(weapon);
            sendMessageToAllParticipants(
                    new GameMessage(attacker.getColorTaggedName() + " attacks " + a.getToHit() + ' '
                            + target.getColorTaggedName() + "!"));
            if (target.getStats().get(Stats.AC) > a.getToHit().getTotal()) { // misses
                sendMessageToAllParticipants(new MissMessage(attacker, target, a));
            } else {
                sendMessageToAllParticipants(new GameMessage(target.applyAttack(a)));
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
        StringBuilder sb = new StringBuilder();

        sb.append("Battle Participants:\r\n");
        for (Creature c : participants) {
            sb.append(c.getColorTaggedName()).append("\r\n");
        }
        sb.append("\r\n");
        sb.append("Up Next: ");
        Creature c = getCurrent();
        sb.append(c.getColorTaggedName()).append("\r\n");
        return sb.toString();
    }

    @Override
    public String printDescription() {
        return this.toString();
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
                    ctx.sendMsg(new SeeOutMessage(this));
                    handled = true;
                } else if (type == CommandMessage.GO) {
                    handled = this.handleGo(ctx, msg);
                } else if (type == CommandMessage.INTERACT) {
                    ctx.sendMsg(new SingleHelpMessage(this.interceptorCmds, type));
                    handled = true;
                } else if (type == CommandMessage.TAKE) {
                    ctx.sendMsg(new SingleHelpMessage(this.interceptorCmds, type));
                    handled = true;
                }
            }
            if (handled) {
                return handled;
            }
        }
        ctx.setBattleManager(this);
        return MessageHandler.super.handleMessage(ctx, msg);
    }

    private Boolean handleGo(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.GO) {
            Integer check = 10 + this.participants.size();
            RollResult result = ctx.getCreature().check(Attributes.DEX);
            if (result.getTotal() < check) {
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
        System.out.println(ctx.getCreature().toString() + " attempts attacking " + aMessage.getTarget());

        Creature attacker = ctx.getCreature();

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

        Creature targetCreature = null;
        List<Creature> possTargets = this.room.getCreaturesInRoom(aMessage.getTarget());
        if (possTargets.size() == 1) {
            targetCreature = possTargets.get(0);
        }
        if (targetCreature == null) {
            if (possTargets.size() == 0) {
                ctx.sendMsg(new BadTargetSelectedMessage(BadTargetOption.DNE, aMessage.getTarget(), possTargets));
            } else {
                ctx.sendMsg(new BadTargetSelectedMessage(BadTargetOption.UNCLEAR, aMessage.getTarget(), possTargets));
            }
            return true;
        }

        AttackAction attackAction = new AttackAction(targetCreature, weapon);
        this.takeAction(ctx.getCreature(), attackAction);
        return true;
    }

    private void callReinforcements(Creature attackingCreature, Creature targetCreature) {
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
