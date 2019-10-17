package com.lhf.game.map.objects;

import com.lhf.game.map.objects.interfaces.Usable;

public class Switch extends RoomObject implements Usable {
    public Switch(String name, boolean isVisible) {
        super(name, isVisible);
    }

    @Override
    public String doUseAction() {
        return null;
    }
}
