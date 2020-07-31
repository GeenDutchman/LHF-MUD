package com.lhf.game.map.objects.roomobject;

import com.lhf.game.item.Item;
import com.lhf.game.map.objects.roomobject.abstracts.RoomObject;
import com.lhf.game.map.objects.roomobject.interfaces.Container;

public class Corpse extends RoomObject implements Container {
    public Corpse(String name, boolean isVisible) {
        super(name, isVisible, "This is " + name + ".  They are quite clearly dead.  You can't quite tell the cause...");
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
