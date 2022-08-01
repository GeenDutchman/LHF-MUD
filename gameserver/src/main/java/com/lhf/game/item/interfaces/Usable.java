package com.lhf.game.item.interfaces;

import java.util.HashMap;
import java.util.Map;

import com.lhf.game.creature.Creature;
import com.lhf.game.item.Item;
import com.lhf.game.map.Room;
import com.lhf.messages.out.SeeOutMessage;

public abstract class Usable extends Takeable {
    private Integer numCanUseTimes;
    private Integer hasBeenUsedTimes = 0;
    private Map<String, UseAction> methods;

    public Usable(String name, boolean isVisible) {
        super(name, isVisible);
        methods = new HashMap<>();
        numCanUseTimes = 1;
    }

    /**
     * Create a new Usable object.
     *
     * @param name           The name to give the object
     * @param isVisible      Set if it is visible
     * @param useSoManyTimes if > 0 then can use that many times, if < 0 then has
     *                       infinite uses
     */
    public Usable(String name, boolean isVisible, int useSoManyTimes) {
        super(name, isVisible);
        methods = new HashMap<>();
        numCanUseTimes = useSoManyTimes;
    }

    protected void setUseAction(String whenItIsThis, UseAction doThis) {
        methods.put(whenItIsThis, doThis);
    }

    protected void removeUseAction(String targetName) {
        methods.remove(targetName);
    }

    public boolean hasUsesLeft() {
        return (numCanUseTimes < 0) || (hasBeenUsedTimes < numCanUseTimes);
    }

    public String doUseAction(Object usingOn) {
        if (methods == null || usingOn == null) {
            return "You cannot use this like that!";
        }
        if (!hasUsesLeft()) {
            return "This item has been used up.";
        }

        UseAction method = null;
        if (usingOn instanceof Item) {
            // specific to that Item
            method = methods.get(((Item) usingOn).getName());
            if (method == null) {
                // general to all Items
                method = methods.get(Item.class.getName());
            }
        } else if (usingOn instanceof Creature) {
            // specific to that Creature
            method = methods.get(((Creature) usingOn).getName());
            if (method == null) {
                // specific to CreatureType
                method = methods.get(((Creature) usingOn).getFaction().toString());
            }
            if (method == null) {
                // general to all Creatures
                method = methods.get(Creature.class.getName());
            }
        } else if (usingOn instanceof Room) {
            // specific to that Room
            method = methods.get(((Room) usingOn).getName());
            if (method == null) {
                method = methods.get(Room.class.getName());
            }
        }

        if (method == null) {
            return "You cannot use " + this.getColorTaggedName() + " on that!";
        }

        if (numCanUseTimes > 0) {
            hasBeenUsedTimes++;
        }

        return method.useAction(usingOn);
    }

    @Override
    public SeeOutMessage produceMessage() {
        SeeOutMessage seeOutMessage = new SeeOutMessage(this);
        return seeOutMessage;
    }

}
