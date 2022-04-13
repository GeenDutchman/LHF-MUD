package com.lhf.game.battle;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.Player;
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.DiceD4;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Item;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.item.interfaces.Weapon;
import com.lhf.game.magic.interfaces.CreatureAffector;
import com.lhf.game.magic.interfaces.DamageSpell;
import com.lhf.game.magic.strategies.CasterVsCreatureStrategy;
import com.lhf.game.map.Room;
import com.lhf.messages.Messenger;
import com.lhf.messages.out.GameMessage;

import java.util.*;
import java.util.logging.Logger;

public class BattleManager {

    private Deque<Creature> participants;
    private Room room;
    private boolean isHappening;
    private Messenger messenger;
    private boolean playerVSplayer;

    public BattleManager(Room room) {
        participants = new ArrayDeque<>();
        this.room = room;
        isHappening = false;
        playerVSplayer = false;
    }

    public void addCreatureToBattle(Creature c) {
        participants.addLast(c);
        c.setInBattle(true);
    }

    public void removeCreatureFromBattle(Creature c) {
        participants.remove(c);
        c.setInBattle(false);
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

    private boolean isCreatureInBattle(Creature c) {
        return participants.contains(c);
    }

    public boolean isBattleOngoing() {
        return isHappening;
    }

    public void startBattle(Creature instigator) {
        isHappening = true;
        messenger.sendMessageToAllInRoom(new GameMessage(instigator.getColorTaggedName() + " started a fight!\r\n"),
                room);
        for (Creature creature : participants) {
            if (creature instanceof Player && instigator != creature) {
                messenger.sendMessageToUser(new GameMessage("You are in the fight!\r\n"), ((Player) creature).getId());
            }
        }
        // If the player started the fight, then it already has an action
        // about to happen. No need to prompt them for it.
        if (!(instigator instanceof Player)) {
            startTurn();
        }
    }

    public void endBattle() {
        for (Creature creature : participants) {
            if (creature instanceof Player) {
                messenger.sendMessageToUser(new GameMessage("Take a deep breath.  You have survived this battle!\r\n"),
                        ((Player) creature).getId());
            }
            creature.setInBattle(false);
        }
        participants.clear();
        messenger.sendMessageToAllInRoom(new GameMessage("The fight is over!\r\n"), room);
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
        messenger.sendMessageToUser(new GameMessage("It is your turn to fight!\r\n"), current.getId());
    }

    public void playerAction(Player p, BattleAction action) {
        if (!participants.contains(p)) {
            // give message that the player is not currently engaged in a fight
            messenger.sendMessageToUser(new GameMessage("You are not currently in a fight.\r\n"), p.getId());
            return;
        }

        if (!isHappening) {
            // give message saying there is no battle ongoing
            messenger.sendMessageToUser(new GameMessage("There is no battle happening.\r\n"), p.getId());
            return;
        }

        if (p != getCurrent()) {
            // give out of turn message
            messenger.sendMessageToUser(new GameMessage("This is not your turn.\r\n"), p.getId());
            return;
        }

        if (action instanceof AttackAction) {
            AttackAction attackAction = (AttackAction) action;

            if (!attackAction.hasTargets()) {
                messenger.sendMessageToUser(new GameMessage("You did not choose any targets.\r\n"), p.getId());
                return;
            }
            List<Creature> targets = attackAction.getTargets();
            for (Creature c : targets) {
                if (!isCreatureInBattle(c)) {
                    // invalid target in list
                    messenger.sendMessageToUser(new GameMessage("One of your targets did not exist.\r\n"), p.getId());
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
                    messenger.sendMessageToUser(new GameMessage("You do not have that weapon.\r\n"), p.getId());
                    return;
                } else if (!(inventoryItem.get() instanceof Weapon)) {
                    messenger.sendMessageToUser(new GameMessage(attackAction.getWeapon() + " is not a Weapon!"),
                            p.getId());
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
                messenger.sendMessageToUser(new GameMessage("You did not choose any targets.\r\n"), p.getId());
                return;
            }
            List<Creature> targets = spell.getTargets();
            for (Creature c : targets) {
                if (!isCreatureInBattle(c)) {
                    // invalid target in list
                    messenger.sendMessageToUser(new GameMessage("One of your targets did not exist.\r\n"), p.getId());
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

    public void setMessenger(Messenger messenger) {
        this.messenger = messenger;
    }

    public void sendMessageToAllParticipants(GameMessage message) {
        for (Creature c : participants) {
            if (c instanceof Player) {
                messenger.sendMessageToUser(message, ((Player) c).getId());
            }
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
}
