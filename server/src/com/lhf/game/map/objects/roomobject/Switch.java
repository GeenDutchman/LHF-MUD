package com.lhf.game.map.objects.roomobject;

import com.lhf.game.map.objects.roomobject.abstractclasses.InteractObject;
import com.lhf.game.map.objects.sharedinterfaces.Examinable;

public class Switch extends InteractObject implements Examinable {
    private String description;

    public Switch(String name, boolean isVisible, boolean isRepeatable, String description) {
        super(name, isVisible, isRepeatable);
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
