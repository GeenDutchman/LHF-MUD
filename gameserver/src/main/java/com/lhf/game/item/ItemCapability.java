package com.lhf.game.item;

import com.lhf.messages.events.SeeEvent;

public interface ItemCapability {
    public static enum ItemCapabilityNames {
        USABLE, LOCKING, EQUIPABLE, RENAMEABLE, WEAPON, INTERACTABLE, CONTAINER, EVENTFUL, PEN;
    }

    public ItemCapabilityNames getCapabilityName();

    public void describe(SeeEvent.ABuilder<?> seeEventBuilder);

    public boolean isStateful();
}