package com.lhf.game.item.concrete;

import com.lhf.game.events.messages.out.SeeOutMessage;
import com.lhf.game.item.InteractObject;

public class Dispenser extends InteractObject {
    private int count;

    public Dispenser(String name, boolean isVisible, boolean isRepeatable, String description) {
        super(name, isVisible, isRepeatable, description);
        count = 0;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        count++;
    }

    @Override
    public String printDescription() {
        // perhaps other things
        return super.printDescription();
    }

    @Override
    public SeeOutMessage produceMessage() {
        SeeOutMessage.Builder seeOutMessage = SeeOutMessage.getBuilder().setExaminable(this);
        return seeOutMessage.Build();
    }
}
