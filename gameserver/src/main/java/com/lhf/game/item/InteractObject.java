package com.lhf.game.item;

import com.lhf.game.creature.ICreature;
import com.lhf.game.item.interfaces.InteractAction;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.ItemInteractionEvent;
import com.lhf.messages.events.ItemInteractionEvent.InteractOutMessageType;

import java.util.HashMap;
import java.util.Map;

public class InteractObject extends Item {
    private Map<String, Object> interactItems;
    private InteractAction method = null;
    // Indicates if the action can be used multiple times
    protected boolean isRepeatable;
    // Indicates if an interaction has already happened
    protected boolean hasBeenInteracted = false;

    public InteractObject(String name, boolean isVisible, boolean isRepeatable, String description) {
        super(name, isVisible, description);
        interactItems = new HashMap<>();
        this.isRepeatable = isRepeatable;
    }

    @Override
    public InteractObject makeCopy() {
        return new InteractObject(this.getName(), this.checkVisibility(), isRepeatable, descriptionString);
    }

    public void setAction(InteractAction interactMethod) {
        method = interactMethod;
    }

    public void setItem(String key, Object obj) {
        interactItems.put(key, obj);
    }

    public GameEvent doUseAction(ICreature creature) {
        if (method == null) {
            return ItemInteractionEvent.getBuilder().setTaggable(this).setSubType(InteractOutMessageType.NO_METHOD)
                    .Build();
        }
        if (!isRepeatable && hasBeenInteracted) {
            return ItemInteractionEvent.getBuilder().setTaggable(this).setSubType(InteractOutMessageType.USED_UP)
                    .Build();
        }
        hasBeenInteracted = true;
        return method.doAction(creature, this, interactItems);
    }

    @Override
    public String printDescription() {
        String otherDescription = super.printDescription();
        if (hasBeenInteracted) {
            otherDescription += " It looks like it has been interacted with already, it might not work again.";
        }
        return otherDescription;
    }

    @Override
    public String getStartTag() {
        return "<interactable>";
    }

    @Override
    public String getEndTag() {
        return "</interactable>";
    }
}
