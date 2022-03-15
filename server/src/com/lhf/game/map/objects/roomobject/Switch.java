package com.lhf.game.map.objects.roomobject;

import com.lhf.game.map.objects.roomobject.abstracts.InteractObject;

public class Switch extends InteractObject {
    public Switch(String name, boolean isVisible, boolean isRepeatable, String description) {
        super(name, isVisible, isRepeatable, description);
    }
}
