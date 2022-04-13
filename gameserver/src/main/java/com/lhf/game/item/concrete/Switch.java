package com.lhf.game.item.concrete;

import com.lhf.game.item.interfaces.InteractObject;

public class Switch extends InteractObject {
    public Switch(String name, boolean isVisible, boolean isRepeatable, String description) {
        super(name, isVisible, isRepeatable, description);
    }
}
