package com.lhf.messages.out;

public class FightOverMessage extends OutMessage {
    private boolean addressSingle;

    public FightOverMessage(boolean addressSingle) {
        this.addressSingle = addressSingle;
    }

    @Override
    public String toString() {
        if (this.addressSingle) {
            return "Take a deep breath.  You have survived this battle!";
        }
        return "The fight is over!";
    }

}
