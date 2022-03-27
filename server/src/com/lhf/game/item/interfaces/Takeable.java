package com.lhf.game.item.interfaces;

import com.lhf.game.item.Item;

public abstract class Takeable extends Item {

    public Takeable(String name, boolean isVisible) {
        super(name, isVisible);
    }

    public Takeable(String name, boolean isVisible, String description) {
        super(name, isVisible, description);
    }

}
