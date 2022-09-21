package com.lhf.game;

import java.util.Optional;

import com.lhf.Examinable;
import com.lhf.game.item.Item;

public interface Container extends Examinable {
    boolean addItem(Item item);

    Optional<Item> getItem(String name);

    Optional<Item> removeItem(String name);

    boolean removeItem(Item item);

    boolean hasItem(String name);
}
