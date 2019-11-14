package com.lhf.game.map.objects.roomobject.abstractclasses;

import com.lhf.game.map.objects.sharedinterfaces.Taggable;

public abstract class RoomObject implements Taggable {
    private String objectName;
    private boolean isVisible;

    public RoomObject(String name, boolean isVisible) {
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
        if (objectName.equalsIgnoreCase(name)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RoomObject)) {
            return false;
        }
        RoomObject ro = (RoomObject)obj;
        return objectName.equals(ro.objectName);
    }

    @Override
    public String getStartTagName() {
        return "<object>";
    }

    @Override
    public String getEndTagName() {
        return "</object>";
    }
}
