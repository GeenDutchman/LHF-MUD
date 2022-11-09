package com.lhf.game.item.concrete;

import java.util.EnumSet;

import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.item.InteractObject;
import com.lhf.game.item.interfaces.InteractAction;
import com.lhf.messages.out.InteractOutMessage;
import com.lhf.messages.out.InteractOutMessage.InteractOutMessageType;

public class Bed extends InteractObject {

    public Bed() {
        super("Bed", true, true, "It's a bed.");
        InteractAction sleepAction = (creature, triggerObject, args) -> {
            if (creature == null) {
                return new InteractOutMessage(triggerObject, InteractOutMessageType.CANNOT);
            }
            EnumSet<Attributes> sleepAttrs = EnumSet.of(Attributes.CON, Attributes.INT);
            Attributes best = creature.getHighestAttributeBonus(sleepAttrs);
            MultiRollResult sleepCheck = creature.check(best);
            creature.updateHitpoints(sleepCheck.getTotal());
            // TODO: regain spell energy?
            return new InteractOutMessage(triggerObject, "You slept and got back " + sleepCheck.getColorTaggedName() + " hit points!");
        }
    }

}
