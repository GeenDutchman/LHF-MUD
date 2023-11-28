package com.lhf.game.item.concrete;

import com.lhf.game.creature.inventory.InventoryOwner;

public class Corpse extends Chest {
    public Corpse(String name, boolean isVisible) {
        super(name, isVisible);
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
