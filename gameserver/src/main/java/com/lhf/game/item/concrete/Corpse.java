package com.lhf.game.item.concrete;

import com.lhf.game.ItemContainer;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.inventory.InventoryOwner;

public class Corpse extends Chest {
    public Corpse(String name) {
        super(name);
    }

    public Corpse(ICreature creature, boolean transfer) {
        super(creature != null ? creature.getName() + "'s corpse" : "a corpse");
        if (transfer) {
            ItemContainer.transfer(creature, this, null, false);
        }
    }

    @Override
    public Corpse makeCopy() {
        return new Corpse(this.getName());
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
