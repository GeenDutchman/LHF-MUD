package com.lhf.game.map.objects.roomobject;

import com.lhf.game.map.objects.item.Item;
import com.lhf.game.map.objects.roomobject.abstractclasses.RoomObject;
import com.lhf.game.map.objects.roomobject.interfaces.Container;

public class Corpse extends RoomObject implements Container {
    public Corpse(String name, boolean isVisible) {
        super(name, isVisible);
    }

    @Override
    public boolean addItem(Item item) {
        return false;
    }

    @Override
    public Item takeItem(String name) {
        return null;
    }
}
