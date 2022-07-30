package com.lhf.messages.out;

import com.lhf.game.creature.Creature;
import com.lhf.messages.OutMessageType;

public class BattleTurnMessage extends OutMessage {
    private Creature myTurn;
    private boolean yesTurn;
    private boolean addressTurner;
    private boolean wasted;
    private int wastedPenalty;

    public BattleTurnMessage(Creature myTurn, boolean yesTurn, boolean addressTurner) {
        super(OutMessageType.BATTLE_TURN);
        this.myTurn = myTurn;
        this.yesTurn = yesTurn;
        this.addressTurner = addressTurner;
        this.wasted = false;
        this.wastedPenalty = 0;
    }

    // will only address everyone
    public BattleTurnMessage(Creature myTurn, boolean wasted) {
        super(OutMessageType.BATTLE_TURN);
        this.myTurn = myTurn;
        this.yesTurn = true;
        this.addressTurner = false;
        this.wasted = wasted;
        this.wastedPenalty = 0;
    }

    // will only address everyone
    public BattleTurnMessage(Creature myTurn, boolean wasted, int wastedPenalty) {
        super(OutMessageType.BATTLE_TURN);
        this.myTurn = myTurn;
        this.yesTurn = true;
        this.addressTurner = false;
        this.wasted = wasted;
        this.setPenalty(wastedPenalty);
    }

    public void setPenalty(int wastedPenalty) {
        this.wastedPenalty = wastedPenalty <= 0 ? wastedPenalty : -1 * wastedPenalty;
    }

    public int getPenalty() {
        if (this.wasted) {
            return this.wastedPenalty;
        }
        return 0;
    }

    @Override
    public String toString() {
        if (this.wasted) {
            String penaltyString = this.wastedPenalty > 0
                    ? " They have incurred a penalty of " + Integer.toString(this.wastedPenalty) + " damage!"
                    : "";
            return this.myTurn.getColorTaggedName() + " has wasted their turn!" + penaltyString;
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

    public Creature getMyTurn() {
        return myTurn;
    }

    public boolean isYesTurn() {
        return yesTurn;
    }

    public boolean isAddressTurner() {
        return addressTurner;
    }

    public boolean isWasted() {
        return wasted;
    }

}
