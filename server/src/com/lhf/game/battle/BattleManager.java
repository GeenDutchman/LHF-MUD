package com.lhf.game.battle;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.Player;
import com.lhf.game.item.Item;
import com.lhf.game.item.interfaces.Takeable;
import com.lhf.game.item.interfaces.Weapon;
import com.lhf.game.map.Room;
import com.lhf.game.map.objects.roomobject.Corpse;
import com.lhf.server.messages.Messenger;
import com.lhf.server.messages.out.GameMessage;

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
        List<Creature> dead = new ArrayList<>();
        for (Creature c : participants) {
            if (!c.isAlive()) {
                dead.add(c);
            }
        }
        for (Creature c : dead) {
            removeCreatureFromBattle(c);
            Corpse corpse = c.die();
            room.addObject(corpse);

            for (String i : c.getInventory().getItemList()) {
                Takeable drop = c.dropItem(i).get();
                if (drop instanceof Item) {
                    room.addItem((Item) drop);
                }
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
            if (attackAction.hasWeapon()) {
                if (p.fromAllInventory(attackAction.getWeapon()).isEmpty()) {
                    messenger.sendMessageToUser(new GameMessage("You do not have that weapon.\r\n"), p.getId());
                    return;
                }else if (!(p.fromAllInventory(attackAction.getWeapon()).get() instanceof Weapon)) {
                    messenger.sendMessageToUser(new GameMessage(attackAction.getWeapon() + " is not a Weapon!"), p.getId());
                    return;
                }
            }
            for (Creature c : targets) {
                Attack a = p.attack(attackAction.getWeapon(), c.getName())
                // messenger.sendMessageToAllInRoom(new GameMessage(c.applyAttack(a)),
                // p.getId());
                sendMessageToAllParticipants(new GameMessage(c.applyAttack(a))); // not spam the room
            }
        }
        clearDead();
        if (isBattleOngoing()) {
            nextTurn();
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
            if (c instanceof Player) {
                Player p = (Player) c;
                sb.append(p.getStartTagName());
                sb.append(p.getName());
                sb.append(p.getEndTagName());
                sb.append("\r\n");
            } else {
                sb.append(c.getStartTagName());
                sb.append(c.getName());
                sb.append(c.getEndTagName());
                sb.append("\r\n");
            }
        }
        sb.append("\r\n");
        sb.append("Up Next: ");
        Creature c = getCurrent();
        if (c instanceof Player) {
            Player p = (Player) c;
            sb.append(p.getStartTagName());
            sb.append(p.getName());
            sb.append(p.getEndTagName());
            sb.append("\r\n");
        } else {
            sb.append(c.getStartTagName());
            sb.append(c.getName());
            sb.append(c.getEndTagName());
            sb.append("\r\n");
        }
        return sb.toString();
    }
}
