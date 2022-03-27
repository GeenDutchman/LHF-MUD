package com.lhf.game.item.interfaces;

import com.lhf.game.creature.Player;
import com.lhf.game.item.Item;

import java.util.HashMap;
import java.util.Map;

public abstract class InteractObject extends Item {
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

    public void setAction(InteractAction interactMethod) {
        method = interactMethod;
    }

    public void setItem(String key, Object obj) {
        interactItems.put(key, obj);
    }

    public String doUseAction(Player p) {
        if (method == null) {
            return "Weird, this does nothing at all!  It won't move!";
        }
        if (!isRepeatable && hasBeenInteracted) {
            return "Nothing happened. It appears to already have been interacted with.";
        }
        hasBeenInteracted = true;
        return method.doAction(p, interactItems);
    }

    @Override
    public String getDescription() {
        String otherDescription = super.getDescription();
        if (hasBeenInteracted) {
            otherDescription += " It looks like it has been interacted with already, it might not work again.";
        }
        return otherDescription;
    }

    @Override
    public String getStartTagName() {
        return "<interactable>";
    }

    @Override
    public String getEndTagName() {
        return "</interactable>";
    }
}
