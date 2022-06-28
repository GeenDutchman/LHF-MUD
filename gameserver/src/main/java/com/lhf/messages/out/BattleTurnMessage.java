package com.lhf.messages.out;

public class BattleTurnMessage extends OutMessage {
    private boolean yesTurn;

    public BattleTurnMessage(boolean yesTurn) {
        this.yesTurn = yesTurn;
    }

    @Override
    public String toString() {
        if (this.yesTurn) {
            return "It is your turn to fight!";
        }
        return "It is not your turn!";
    }
}
