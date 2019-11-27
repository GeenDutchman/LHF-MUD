package com.lhf.game.map.objects.roomobject;

import com.lhf.game.map.objects.roomobject.abstracts.InteractObject;

public class Dispenser extends InteractObject {
    private int count;

    public Dispenser(String name, boolean isVisible, boolean isRepeatable) {
        super(name, isVisible, isRepeatable);
        count = 0;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        count++;
    }
}
