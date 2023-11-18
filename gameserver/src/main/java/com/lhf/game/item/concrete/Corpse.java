package com.lhf.game.item.concrete;

// TODO: #129 actually use this

public class Corpse extends Chest {
    public Corpse(String name, boolean isVisible) {
        super(name, isVisible);
    }

    @Override
    public String printDescription() {
        return "This is " + this.getColorTaggedName()
                + ".  They are quite clearly dead.  You can't quite tell the cause...";
    }

}
