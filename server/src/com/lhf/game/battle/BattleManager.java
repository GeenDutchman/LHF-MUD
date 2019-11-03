package com.lhf.game.battle;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.Player;
import com.lhf.game.map.Room;

import java.util.ArrayDeque;
import java.util.Deque;

public class BattleManager {

    private Deque<Creature> participants;
    private Room room;


    public BattleManager(Room room) {
        participants = new ArrayDeque<>();
        this.room = room;
    }

    public void addCreatureToBattle(Creature c) {
        participants.addLast(c);
        if (participants.size() == 2) {
            startTurn();
        }
    }

    public void removeCreatureFromBattle(Creature c) {
        participants.remove(c);
    }

    public boolean isPlayerInBattle(Player p) {
        return participants.contains(p);
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
            promptPlayerToAct((Player)current);
        }
        else {
            performAiTurn(current);
        }
    }

    private void performAiTurn(Creature current) {
        //wait for a couple seconds for "realism"
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Do creature's turn

        nextTurn();
    }

    private void promptPlayerToAct(Player current) {
        //send message to player that it is their turn
    }

    public void playerAction(Player p, BattleAction action) {
        if (!participants.contains(p)) {
            //give message that the player is not currently engaged in a fight
            return;
        }

        if (participants.size() < 2) {
            //give message saying there is no one to fight
            return;
        }

        if (p != getCurrent()) {
            //give out of turn message
            return;
        }

        if (action instanceof AttackAction) {
            AttackAction attackAction = (AttackAction)action;
            //do attack action
                //verify target exists
                //generate attack object
                //apply to target
                //if target dies, kill it
                //if anything was incorrect with the command, just message and return
        }
        //check for other possible actions

        //action successfully done
        nextTurn();
    }

    private void creatureDied(Creature c) {
        //perform death and remove from battle
        //generate and add corpse to room
    }

    private Creature getCurrent() {
        return participants.getFirst();
    }
}
