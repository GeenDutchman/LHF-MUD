package com.lhf.game.map.objects.roomobject.abstracts;

import com.lhf.game.creature.Player;
import com.lhf.game.map.objects.roomobject.interfaces.InteractAction;

import java.util.HashMap;
import java.util.Map;

public abstract class InteractObject extends RoomObject{
    private Map<String, Object> interactItems;
    private InteractAction method = null;
    //Indicates if the action can be used multiple times
    private boolean isRepeatable;
    //Indicates if an interaction has already happened
    private boolean hasBeenInteracted = false;
    public InteractObject(String name, boolean isVisible, boolean isRepeatable) {
        super(name, isVisible);
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
            return null;
        }
        if (!isRepeatable && hasBeenInteracted) {
            return "Nothing happened. It appears to already have been interacted with.";
        }
        hasBeenInteracted = true;
        return method.doAction(p, interactItems);
    }
}
