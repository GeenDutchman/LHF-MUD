package com.lhf.game.item;

import java.io.Serializable;

import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.messages.events.SeeEvent;

public interface ItemCapability extends Serializable {
    public static enum ItemCapabilityNames {
        USABLE, LOCKING, EQUIPABLE, RENAMEABLE, WEAPON, INTERACTABLE, CONTAINER, EVENTFUL, PEN, MODIFIER;
    }

    public ItemCapabilityNames getCapabilityName();

    public void describe(SeeEvent.ABuilder<?> seeEventBuilder);

    public boolean isStateful();

    public static interface ICapabilityDelta {
        public void buildOutput(RichOutputBuilder builder);
    }
}