package com.lhf.game.item.concrete;

import com.lhf.game.item.InteractObject;

public class Switch extends InteractObject {
    public Switch(String name, boolean isVisible, boolean isRepeatable, String description) {
        super(name, isVisible, isRepeatable, description);
    }

    @Override
    public Switch makeCopy() {
        return new Switch(this.getName(), this.checkVisibility(), this.isRepeatable, descriptionString);
    }
}
