package com.lhf.game.creature.inventory;

import com.lhf.game.item.interfaces.Takeable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Inventory {
    private ArrayList<Takeable> items;

    public Inventory() {
        items = new ArrayList<>();
    }

    public void addItem(Takeable i) {
        this.items.add(i);
    }

    public boolean hasItem(Takeable i) {
        return this.items.contains(i);
    }

    public boolean hasItem(String itemName) {
        return this.items.stream().anyMatch(i -> i.CheckNameRegex(itemName, 3));
    }

    public Optional<Takeable> getItem(String itemName) {
        for (Takeable exact : this.items) {
            if (exact.checkName(itemName)) {
                return Optional.of(exact);
            }
        }
        Optional<Takeable> matched = this.items.stream().filter(i -> i.CheckNameRegex(itemName, 3)).findAny();
        return matched;
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

    public void removeItem(Takeable item) {
        this.items.remove(item);
    }
}
