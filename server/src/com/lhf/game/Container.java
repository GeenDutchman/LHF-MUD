package com.lhf.game;

import java.util.Optional;

import com.lhf.game.item.Item;

public interface Container {
    boolean addItem(Item item);

    Optional<Item> getItem(String name);

    Optional<Item> removeItem(String name);

    boolean hasItem(String name);
}
