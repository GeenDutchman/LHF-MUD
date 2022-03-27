package com.lhf.game.item.concrete;

import java.util.Optional;

import com.lhf.game.Container;
import com.lhf.game.item.Item;

// TODO: actually use this

public class Corpse extends Item implements Container {
    public Corpse(String name, boolean isVisible) {
        super(name, isVisible);
    }

    @Override
    public boolean addItem(Item item) {
        return false;
    }

    @Override
    public String getDescription() {
        return "This is " + this.getColorTaggedName()
                + ".  They are quite clearly dead.  You can't quite tell the cause...";
    }

    @Override
    public Optional<Item> getItem(String name) {
        return Optional.empty();
    }

    @Override
    public Optional<Item> removeItem(String name) {
        return Optional.empty();
    }

    @Override
    public boolean hasItem(String name) {
        return false;
    }

}
