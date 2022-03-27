package com.lhf.game.creature.inventory;

import com.lhf.game.Container;
import com.lhf.game.item.Item;
import com.lhf.game.item.interfaces.Takeable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Inventory implements Container {
    private ArrayList<Takeable> items;

    public Inventory() {
        items = new ArrayList<>();
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
        return this.items.stream().anyMatch(i -> i.CheckNameRegex(itemName, 3));
    }

    public Optional<Item> getItem(String itemName) {
        for (Takeable exact : this.items) {
            if (exact.checkName(itemName)) {
                return Optional.of((Item) exact);
            }
        }
        Optional<Takeable> matched = this.items.stream().filter(i -> i.CheckNameRegex(itemName, 3)).findAny();
        if (matched.isPresent()) {
            return Optional.of((Item) matched.get());
        }
        return Optional.empty();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    @Override
    public String toString() {
        return this.items.stream().map(item -> "<item>" + item.getName() + "</item>").collect(Collectors.joining(", "));
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

    public boolean removeItem(Takeable item) {
        return this.items.remove(item);
    }

    public boolean removeItem(String name) {
        for (Takeable exact : this.items) {
            if (exact.checkName(name)) {
                return this.items.remove(exact);
            }
        }
        return false;
    }
}
