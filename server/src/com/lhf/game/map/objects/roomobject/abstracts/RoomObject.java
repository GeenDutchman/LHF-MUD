package com.lhf.game.map.objects.roomobject.abstracts;

import com.lhf.game.map.objects.sharedinterfaces.Examinable;
import com.lhf.game.map.objects.sharedinterfaces.Taggable;

public abstract class RoomObject implements Taggable, Examinable {
    private String objectName;
    private boolean isVisible;
    private String description;

    public RoomObject(String name, boolean isVisible, String description) {
        this.objectName = name;
        this.isVisible = isVisible;
        this.description = description;
    }

    public boolean checkVisibility() {
        return isVisible;
    }

    public String getName() {
        return objectName;
    }

    public boolean checkName(String name) {
        return objectName.equalsIgnoreCase(name);
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

    @Override
    public String getDescription() {
        return this.description;
    }
}
