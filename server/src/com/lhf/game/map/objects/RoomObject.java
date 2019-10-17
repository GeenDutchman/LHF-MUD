package com.lhf.game.map.objects;

public abstract class RoomObject {

    String objectName;
    boolean isVisible;

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

}
