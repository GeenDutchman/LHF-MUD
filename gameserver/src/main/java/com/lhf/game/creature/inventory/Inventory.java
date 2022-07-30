package com.lhf.game.creature.inventory;

import com.lhf.game.Container;
import com.lhf.game.item.Item;
import com.lhf.game.item.interfaces.Takeable;
import com.lhf.messages.out.InventoryOutMessage;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.messages.out.SeeOutMessage.SeeCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Inventory implements Container {
    private List<Takeable> items;

    public Inventory() {
        items = new ArrayList<>();
    }

    @Override
    public String getName() {
        return "Inventory";
    }

    public boolean addItem(Item i) {
        if (i instanceof Takeable) {
            this.items.add((Takeable) i);
            return true;
        }
        return false;
    }

    public boolean hasItem(Takeable i) {
        return this.items.contains(i);
    }

    public boolean hasItem(String itemName) {
        return this.items.stream().anyMatch(item -> item.CheckNameRegex(itemName, 3));
    }

    public Optional<Item> getItem(String itemName) {
        Optional<Takeable> takeOpt = this.items.stream().filter(item -> item.CheckNameRegex(itemName, 3)).findAny();
        if (takeOpt.isPresent()) {
            return Optional.of((Item) takeOpt.get());
        }
        return Optional.empty();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    @Override
    public String toString() {
        return this.items.stream().map(item -> item.getColorTaggedName()).collect(Collectors.joining(", "));
    }

    public InventoryOutMessage getInventoryOutMessage() {
        return new InventoryOutMessage(this.items);
    }

    public String toStoreString() {
        return this.items.stream().map(Takeable::getName).collect(Collectors.joining(", "));
    }

    public List<String> getItemList() {
        List<String> names = new ArrayList<>();
        for (Takeable t : items) {
            names.add(t.getName());
        }
        return names;
    }

    public boolean removeItem(Item item) {
        return this.items.remove(item);
    }

    public Optional<Item> removeItem(String name) {
        for (Item exact : this.items) {
            if (exact.checkName(name)) {
                this.items.remove(exact);
                return Optional.of(exact);
            }
        }
        return Optional.empty();
    }

    @Override
    public String printDescription() {
        return "This is your inventory.";
    }

    @Override
    public SeeOutMessage produceMessage() {
        SeeOutMessage seeOutMessage = new SeeOutMessage(this);
        for (Takeable thing : this.items) {
            seeOutMessage.addSeen(SeeCategory.TAKEABLE, thing);
        }
        return seeOutMessage;
    }
}
