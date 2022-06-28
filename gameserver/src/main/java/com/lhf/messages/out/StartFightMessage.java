package com.lhf.messages.out;

import com.lhf.game.creature.Creature;

public class StartFightMessage extends OutMessage {
    private Creature instigator;
    private boolean singleAddress;

    public StartFightMessage(Creature instigator, boolean singleAddress) {
        this.instigator = instigator;
        this.singleAddress = singleAddress;
    }

    @Override
    public String toString() {
        if (this.singleAddress) {
            return "You are in the fight!";
        }
        return this.instigator.getColorTaggedName() + " started a fight!";
    }

    public Creature getInstigator() {
        return instigator;
    }

}
