package com.lhf.game.map.objects.roomobject.interfaces;

import com.lhf.game.item.Item;

public interface Container {
    boolean addItem(Item item);
    Item takeItem(String name);
}
