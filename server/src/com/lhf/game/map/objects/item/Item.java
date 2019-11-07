package com.lhf.game.map.objects.item;

import com.lhf.game.map.objects.sharedinterfaces.Examinable;

public abstract class Item implements Examinable {

    //Name it will be known by
    private String objectName;
    //Will not output with look if false
    private boolean isVisible;

    public Item(String name, boolean isVisible) {
        this.objectName = name;
        this.isVisible = isVisible;
    }

    public boolean checkVisibility() {
        return isVisible;
    }

    public String getName() {
        return objectName;
    }

    public boolean checkName(String name) {
        if (objectName.equals(name)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Item)) {
            return false;
        }
        Item ro = (Item)obj;
        return objectName.equals(ro.objectName);
    }
}
