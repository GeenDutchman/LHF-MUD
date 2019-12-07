package com.lhf.game.creature.inventory;

import com.lhf.game.item.interfaces.Takeable;

import java.util.ArrayList;
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
        return this.items.stream().anyMatch(i -> i.getName().equalsIgnoreCase(itemName));
    }

    public Optional<Takeable> getItem(String itemName) {
        return this.items.stream().filter(i -> i.getName().equalsIgnoreCase(itemName)).findAny();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    @Override
    public String toString() {
        return this.items.stream().map(item -> "<item>" + item.getName() + "</item>").collect(Collectors.joining(", "));
    }

    public String toStoreString(){
        return this.items.stream().map(Takeable::getName).collect(Collectors.joining(", "));
    }

    public void removeItem(Takeable item) {
        this.items.remove(item);
    }
}