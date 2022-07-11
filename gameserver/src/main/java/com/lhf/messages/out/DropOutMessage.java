package com.lhf.messages.out;

import com.lhf.Taggable;

public class DropOutMessage extends OutMessage {
    private Taggable item;

    public DropOutMessage(Taggable item) {
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
