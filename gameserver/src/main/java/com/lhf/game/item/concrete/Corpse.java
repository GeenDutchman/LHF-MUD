package com.lhf.game.item.concrete;

import com.lhf.game.ItemContainer;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.inventory.InventoryOwner;

public class Corpse extends Chest {
    public Corpse(String name, boolean isVisible) {
        super(name, isVisible, false, true);
    }

    public Corpse(ICreature creature, boolean transfer) {
        super(creature != null ? creature.getName() + "'s corpse" : "a corpse", true, false, true);
        if (transfer) {
            ItemContainer.transfer(creature, this, null, false);
        }
    }

    @Override
    public Corpse makeCopy() {
        return new Corpse(this.getName(), this.isVisible());
    }

    @Override
    public String printDescription() {
        return "This is " + this.getColorTaggedName()
                + ".  They are quite clearly dead.  You can't quite tell the cause...";
    }

    @Override
    public boolean isUnlocked() {
        return true;
    }

    @Override
    public boolean isAuthorized(InventoryOwner attemtper) {
        return true;
    }

}
