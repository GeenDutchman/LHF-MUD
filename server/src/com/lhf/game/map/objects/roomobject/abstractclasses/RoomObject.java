package com.lhf.game.map.objects.roomobject.abstractclasses;

public abstract class RoomObject {
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
        if (objectName.equals(name)) {
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
}
