package com.lhf.messages.out;

import com.lhf.messages.OutMessageType;

public class FightOverMessage extends OutMessage {
    private boolean addressSingle;

    public FightOverMessage(boolean addressSingle) {
        super(OutMessageType.FIGHT_OVER);
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
