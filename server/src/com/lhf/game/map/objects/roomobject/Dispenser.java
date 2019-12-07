package com.lhf.game.map.objects.roomobject;

import com.lhf.game.map.objects.roomobject.abstracts.InteractObject;

public class Dispenser extends InteractObject {
    private int count;

    public Dispenser(String name, boolean isVisible, boolean isRepeatable, String description) {
        super(name, isVisible, isRepeatable, description);
        count = 0;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        count++;
    }

    @Override
    public String getDescription() {
        // perhaps other things
        return super.getDescription();
    }
}
