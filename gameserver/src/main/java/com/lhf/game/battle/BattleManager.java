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

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.Player;
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.dice.DiceD4;
import com.lhf.game.enums.Attributes;
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
import com.lhf.messages.out.GameMessage;

public class BattleManager implements MessageHandler {

    private Deque<Creature> participants;
    private Room room;
    private boolean isHappening;
    private boolean playerVSplayer;
    private MessageHandler successor;
    private Map<CommandMessage, String> interceptorCmds;
    private Map<CommandMessage, String> cmds;

    public BattleManager(Room room) {
        participants = new ArrayDeque<>();
        this.room = room;
        isHappening = false;
        playerVSplayer = false;
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
        participants.addLast(c);
        c.setInBattle(true);
        c.setSuccessor(this);
    }

    public void removeCreatureFromBattle(Creature c) {
        participants.remove(c);
        c.setInBattle(false);
        c.setSuccessor(this.successor);
        if (!playerVSplayer && !hasNonPlayerInBattle()) { // not pvp and no monsters
            endBattle();
        } else if (!hasPlayerInBattle() || participants.size() <= 1) { // pvp and only one survivor who did not flee OR
                                                                       // just monsters
            endBattle();
        }
    }

    public boolean hasPlayerInBattle() {
        for (Creature creature : participants) {
            if (creature instanceof Player) {
                return true;
            }
        }
        return false;
    }

    public boolean hasNonPlayerInBattle() {
        for (Creature creature : participants) {
            if (!(creature instanceof Player)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlayerInBattle(Player p) {
        return participants.contains(p);
    }

    public boolean isCreatureInBattle(Creature c) {
        return participants.contains(c);
    }

    public boolean isBattleOngoing() {
        return isHappening;
    }

    public void startBattle(Creature instigator) {
        isHappening = true;
        this.room.sendMessageToAll(new GameMessage(instigator.getColorTaggedName() + " started a fight!\r\n"));
        for (Creature creature : participants) {
            creature.sendMsg(new GameMessage("You are in the fight!\r\n"));
        }
        // If the player started the fight, then it already has an action
        // about to happen. No need to prompt them for it.
        if (!(instigator instanceof Player)) {
            startTurn();
        }
    }

    public void endBattle() {
        for (Creature creature : participants) {
            creature.sendMsg(new GameMessage("Take a deep breath.  You have survived this battle!\r\n"));
            creature.setInBattle(false);
        }
        participants.clear();
        this.room.sendMessageToAll(new GameMessage("The fight is over!\r\n"));
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
            promptPlayerToAct((Player) current);
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

    private void promptPlayerToAct(Player current) {
        // send message to player that it is their turn
        current.sendMsg(new GameMessage("It is your turn to fight!\r\n"));
    }

    public void playerAction(Player p, BattleAction action) {
        if (!participants.contains(p)) {
            // give message that the player is not currently engaged in a fight
            p.sendMsg(new GameMessage("You are not currently in a fight.\r\n"));
            return;
        }

        if (!isHappening) {
            // give message saying there is no battle ongoing
            p.sendMsg(new GameMessage("There is no battle happening.\r\n"));
            return;
        }

        if (p != getCurrent()) {
            // give out of turn message
            p.sendMsg(new GameMessage("This is not your turn.\r\n"));
            return;
        }

        if (action instanceof AttackAction) {
            AttackAction attackAction = (AttackAction) action;

            if (!attackAction.hasTargets()) {
                p.sendMsg(new GameMessage("You did not choose any targets.\r\n"));
                return;
            }
            List<Creature> targets = attackAction.getTargets();
            for (Creature c : targets) {
                if (!isCreatureInBattle(c)) {
                    // invalid target in list
                    p.sendMsg(new GameMessage("One of your targets did not exist.\r\n"));
                    return;
                }
                if (c instanceof Player && !playerVSplayer) {
                    this.playerVSplayer = true;
                }
            }
            Weapon weapon;
            if (attackAction.hasWeapon()) {
                Optional<Item> inventoryItem = p.getItem(attackAction.getWeapon());
                if (inventoryItem.isEmpty()) {
                    p.sendMsg(new GameMessage("You do not have that weapon.\r\n"));
                    return;
                } else if (!(inventoryItem.get() instanceof Weapon)) {
                    p.sendMsg(new GameMessage(attackAction.getWeapon() + " is not a Weapon!"));
                    return;
                } else {
                    weapon = (Weapon) inventoryItem.get();
                }
            } else {
                weapon = p.getWeapon();
            }
            applyAttacks(p, weapon, targets);
        } else if (action instanceof CreatureAffector) {
            CreatureAffector spell = (CreatureAffector) action;
            if (!spell.hasTargets()) {
                p.sendMsg(new GameMessage("You did not choose any targets.\r\n"));
                return;
            }
            List<Creature> targets = spell.getTargets();
            for (Creature c : targets) {
                if (!isCreatureInBattle(c)) {
                    // invalid target in list
                    p.sendMsg(new GameMessage("One of your targets did not exist.\r\n"));
                    return;
                }
                if (c instanceof Player && !playerVSplayer && spell instanceof DamageSpell) {
                    this.playerVSplayer = true;
                }
            }
            applySpell((Creature) spell.getCaster(), (CreatureAffector) spell, targets);
        } else {
            sendMessageToAllParticipants(new GameMessage(p.getColorTaggedName() + " wasted their turn!"));
        }
        endTurn();
    }

    private void announceMiss(Creature attacker, Creature target, Attack attack) {
        StringBuilder output = new StringBuilder();
        Dice chooser = new DiceD4(1);
        int which = chooser.rollDice().getTotal();
        switch (which) {
            case 1:
                output.append(attack.getTaggedAttacker()).append(' ').append(attack.getToHit()).append(" misses ")
                        .append(target.getColorTaggedName());
                break;
            case 2:
                output.append(target.getColorTaggedName()).append(" dodged the attack ").append(attack.getToHit())
                        .append(" from ")
                        .append(attack.getTaggedAttacker());
                break;
            case 3:
                output.append(attack.getTaggedAttacker()).append(" whiffed ").append(attack.getToHit())
                        .append(" their attack on ")
                        .append(target.getColorTaggedName());
                break;
            default:
                output.append("The attack ").append(attack.getToHit()).append(" by ").append(attack.getTaggedAttacker())
                        .append(" on ")
                        .append(target.getColorTaggedName()).append(" does not land");
                break;

        }
        output.append('\n');
        sendMessageToAllParticipants(new GameMessage(output.toString()));
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
            if (target.getStats().get(Stats.AC) > a.getToHit().getTotal()) {
                announceMiss(attacker, target, a);
            } else {
                sendMessageToAllParticipants(new GameMessage(target.applyAttack(a)));
            }
        }
    }

    private Creature getCurrent() {
        return participants.getFirst();
    }

    public void sendMessageToAllParticipants(GameMessage message) {
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
                    ctx.sendMsg(new GameMessage(this.toString()));
                    handled = true;
                } else if (type == CommandMessage.GO) {
                    handled = this.handleGo(ctx, msg);
                } else if (type == CommandMessage.INTERACT) {
                    ctx.sendMsg(new GameMessage(this.interceptorCmds.get(type)));
                    handled = true;
                } else if (type == CommandMessage.TAKE) {
                    ctx.sendMsg(new GameMessage(this.interceptorCmds.get(type)));
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
                this.room.sendMessageToAllExcept(new GameMessage(
                        ctx.getCreature().getColorTaggedName() + " attempted " + result.getColorTaggedName()
                                + " to flee!"),
                        ctx.getCreature().getName());
                ctx.sendMsg(new GameMessage("You were not " + result.getColorTaggedName() + " able to flee"));
                return true;
            }
            this.room.sendMessageToAllExcept(new GameMessage(
                    ctx.getCreature().getColorTaggedName() + " flees " + result.getColorTaggedName() + " the battle!"),
                    ctx.getCreature().getName());
        }
        return MessageHandler.super.handleMessage(ctx, msg);
    }

    private Boolean handleAttack(CommandContext ctx, AttackMessage aMessage) {
        System.out.println(ctx.getCreature().toString() + " attempts attacking " + aMessage.getTarget());
        Creature targetCreature = null;
        List<Creature> possTargets = this.room.getCreaturesInRoom(aMessage.getTarget());
        if (possTargets.size() == 1) {
            targetCreature = possTargets.get(0);
        }
        if (targetCreature == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("You cannot attack '").append(aMessage.getTarget()).append("' ");
            if (possTargets.size() == 0) {
                sb.append("because it does not exist.");
            } else {
                sb.append("because it could be any of these:\n");
                for (Creature c : possTargets) {
                    sb.append(c.getColorTaggedName()).append(" ");
                }
            }
            sb.append("\r\n");
            ctx.sendMsg((new GameMessage(sb.toString())));
            return true;
        }
        String playerName = ctx.getCreature().getName();
        if (targetCreature instanceof Player && targetCreature.getName().equals(playerName)) {
            ctx.sendMsg(new GameMessage("You can't attack yourself!\r\n"));
            return true;
        }
        if (!ctx.getCreature().isInBattle()) {
            this.addCreatureToBattle(ctx.getCreature());
            if (this.isBattleOngoing()) {
                this.room.sendMessageToAllExcept(
                        new GameMessage(ctx.getCreature().getColorTaggedName() + " has joined the ongoing battle!"),
                        ctx.getCreature().getName());
            }
        }

        if (!targetCreature.isInBattle()) {
            this.addCreatureToBattle(targetCreature);
            if (this.isBattleOngoing()) {
                this.room.sendMessageToAllExcept(
                        new GameMessage(targetCreature.getColorTaggedName() + " has joined the ongoing battle!"),
                        targetCreature.getName());
            }
        }

        if (!this.isBattleOngoing()) {
            this.startBattle(ctx.getCreature());
        }
        AttackAction attackAction = new AttackAction(targetCreature, aMessage.getWeapon());
        this.playerAction((Player) ctx.getCreature(), attackAction);
        return true;
    }

}
