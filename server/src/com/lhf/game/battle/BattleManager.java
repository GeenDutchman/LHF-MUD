package com.lhf.game.battle;

import com.lhf.Main;
import com.lhf.game.Attack;
import com.lhf.game.Messenger;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.Player;
import com.lhf.game.map.Room;
import com.lhf.game.map.objects.item.interfaces.Equipable;
import com.lhf.game.shared.enums.EquipmentSlots;
import com.lhf.game.map.objects.item.interfaces.Weapon;
import com.lhf.messages.out.GameMessage;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.logging.Logger;

public class BattleManager {

    private Deque<Creature> participants;
    private Room room;
    private boolean isHappening;
    private Messenger messenger;


    public BattleManager(Room room) {
        participants = new ArrayDeque<>();
        this.room = room;
        isHappening = false;
    }

    public void addCreatureToBattle(Creature c) {
        participants.addLast(c);
        c.setInBattle(true);
    }

    public void removeCreatureFromBattle(Creature c) {
        participants.remove(c);
        c.setInBattle(false);
        if (participants.size() <= 1) { // TODO: Account for when multiple friendlies are in the battle
            Creature creature = participants.poll();
            if (creature instanceof Player) {
                messenger.sendMessageToUser(new GameMessage("Take a deep breath.  You have survived this battle!\r\n"), ((Player) creature).getId());
            }
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
        messenger.sendMessageToAllInRoom(new GameMessage(instigator.getName() + " started a fight!\r\n"), room);
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
            //prompt player to do something
            promptPlayerToAct((Player) current);
        } else if (current instanceof BattleAI){
            AITurn((BattleAI) current);
        } else {
            // Bad juju
            Logger logger = Logger.getLogger(BattleManager.class.getPackageName());
            logger.severe("Trying to perform a turn for something that can't do it\r\n");
        }
    }

    private void AITurn(BattleAI ai) {
        messenger.sendMessageToAllInRoom(new GameMessage(ai.performBattleTurn(participants)), room);
        clearDead();
        if (isBattleOngoing()) {
            nextTurn();
        }
    }

    private void clearDead() {
        for (Creature c: participants) {
            if (!c.isAlive()) {
                removeCreatureFromBattle(c);
            }
        }
    }

    private void promptPlayerToAct(Player current) {
        //send message to player that it is their turn
        messenger.sendMessageToUser(new GameMessage("It is your turn to fight!\r\n"), current.getId());
    }

    public void playerAction(Player p, BattleAction action) {  //TODO: should this return a string?? Note: use messenger class because of way events are set up (Spencer)
        if (!participants.contains(p)) {
            //give message that the player is not currently engaged in a fight
            messenger.sendMessageToUser(new GameMessage("You are not currently in a fight.\r\n"), p.getId());
            return;
        }

        if (!isHappening) {
            //give message saying there is no battle ongoing
            messenger.sendMessageToUser(new GameMessage("There is no battle happening.\r\n"), p.getId());
            return;
        }

        if (p != getCurrent()) {
            //give out of turn message
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
                    //invalid target in list
                    messenger.sendMessageToUser(new GameMessage("One of your targets did not exist.\r\n"), p.getId());
                    return;
                }
            }
            Weapon w;
            if (attackAction.hasWeapon()) {
                if (p.fromAllInventory(attackAction.getWeapon()).isPresent() &&
                        p.fromAllInventory(attackAction.getWeapon()).get() instanceof Weapon) {
                    w = (Weapon) p.fromAllInventory(attackAction.getWeapon()).get();
                } else {
                    //player does not have weapon that he asked to use
                    messenger.sendMessageToUser(new GameMessage("You do not have that weapon.\r\n"), p.getId());
                    return;
                }
            } else {
                w = p.getWeapon();
            }
            Attack a = w.rollAttack();
            a.setAttacker(getCurrent().getColorTaggedName());
            for (Creature c : targets) {
                messenger.sendMessageToAllInRoom(new GameMessage(c.applyAttack(a)), p.getId());
            }
            clearDead();
        }
        if(isBattleOngoing()) {
            nextTurn();
        }
    }

    private void creatureDied(Creature c) {
        //perform death and remove from battle
        //generate and add corpse to room
    }

    private Creature getCurrent() {
        return participants.getFirst();
    }

    public void setMessenger(Messenger messenger) {
        this.messenger = messenger;
    }
}
