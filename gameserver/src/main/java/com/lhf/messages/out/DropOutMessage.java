package com.lhf.messages.out;

import com.lhf.Taggable;
import com.lhf.messages.OutMessageType;

public class DropOutMessage extends OutMessage {
    private Taggable item;

    public DropOutMessage(Taggable item) {
        super(OutMessageType.DROP_OUT);
        this.item = item;
    }

    @Override
    public String toString() {
        return "You glance at your empty hand as the " + item.getColorTaggedName()
                + " drops to the floor.";
    }

    public Taggable getItem() {
        return item;
    }
}
