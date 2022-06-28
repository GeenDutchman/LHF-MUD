package com.lhf.messages.out;

import com.lhf.game.creature.Creature;

public class BattleTurnMessage extends OutMessage {
    private Creature myTurn;
    private boolean yesTurn;
    private boolean addressTurner;
    private boolean wasted;

    public BattleTurnMessage(Creature myTurn, boolean yesTurn, boolean addressTurner) {
        this.myTurn = myTurn;
        this.yesTurn = yesTurn;
        this.addressTurner = addressTurner;
        this.wasted = false;
    }

    // will only address everyone
    public BattleTurnMessage(Creature myTurn, boolean wasted) {
        this.myTurn = myTurn;
        this.yesTurn = true;
        this.addressTurner = false;
        this.wasted = wasted;
    }

    @Override
    public String toString() {
        if (this.wasted) {
            return this.myTurn.getColorTaggedName() + " has wasted their turn!";
        }
        if (this.addressTurner) {
            if (this.yesTurn) {
                return "It is your turn to fight!";
            }
            return "It is not your turn!";
        } else {
            return this.myTurn.getColorTaggedName() + " now has a turn.";
        }
    }

}
