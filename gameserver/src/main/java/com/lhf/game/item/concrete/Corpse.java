package com.lhf.game.item.concrete;

import java.util.Collection;
import java.util.Optional;

import com.lhf.game.ItemContainer;
import com.lhf.game.item.Item;
import com.lhf.messages.out.SeeOutMessage;

// TODO: #129 actually use this

public class Corpse extends Item implements ItemContainer {
    public Corpse(String name, boolean isVisible) {
        super(name, isVisible);
    }

    @Override
    public boolean addItem(Item item) {
        return false;
    }

    @Override
    public String printDescription() {
        return "This is " + this.getColorTaggedName()
                + ".  They are quite clearly dead.  You can't quite tell the cause...";
    }

    @Override
    public SeeOutMessage produceMessage() {
        SeeOutMessage.Builder seeOutMessage = SeeOutMessage.getBuilder().setExaminable(this);
        return seeOutMessage.Build();
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
    public boolean removeItem(Item item) {
        return false;
    }

    @Override
    public boolean hasItem(String name) {
        return false;
    }

    @Override
    public Collection<Item> getItems() {
        return null;
    }

}
