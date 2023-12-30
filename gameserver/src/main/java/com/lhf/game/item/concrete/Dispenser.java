package com.lhf.game.item.concrete;

import com.lhf.game.item.InteractObject;
import com.lhf.messages.events.SeeEvent;

public class Dispenser extends InteractObject {
    private int count;

    public Dispenser(String name, boolean isVisible, boolean isRepeatable, String description) {
        super(name, isVisible, isRepeatable, description);
        count = 0;
    }

    @Override
    public Dispenser makeCopy() {
        return new Dispenser(this.getName(), hasBeenInteracted, isRepeatable, descriptionString);
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
    public SeeEvent produceMessage() {
        SeeEvent.Builder seeOutMessage = SeeEvent.getBuilder().setExaminable(this);
        return seeOutMessage.Build();
    }
}
