package com.lhf.game.inventory;

import com.lhf.game.map.objects.item.interfaces.Takeable;

import java.util.ArrayList;
import java.util.Optional;

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
        return this.items.stream().anyMatch(i -> i.getName().equals(itemName));
    }

    public Optional<Takeable> getItem(String itemName) {
        return this.items.stream().filter(i -> i.getName().equals(itemName)).findAny();
    }

    @Override
    public String toString() {
        return this.items.stream().map(item -> item.getName()).reduce("", (val, acc) -> acc + val + ", ");
    }

    public void removeItem(Takeable item) {
        this.items.remove(item);
    }
}
