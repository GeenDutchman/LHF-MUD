package com.lhf.game.map.objects.roomobject.abstracts;

import java.util.regex.PatternSyntaxException;

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
        return this.getName().equalsIgnoreCase(name);
    }

    public boolean CheckNameRegex(String possName, Integer minimumLength) {
        Integer min = minimumLength;
        if (min < 0) {
            min = 0;
        }
        if (this.getName().length() < min) {
            min = this.getName().length();
        }
        if (min > this.getName().length()) {
            min = this.getName().length();
        }
        if (possName.length() < min || possName.length() > this.getName().length()) {
            return false;
        }
        if (this.checkName(possName)) {
            return true;
        }
        if (possName.matches("[^ a-zA-Z_-]") || possName.contains("*")) {
            return false;
        }
        try {
            return this.getName().matches("(?i).*" + possName + ".*");
        } catch (PatternSyntaxException pse) {
            pse.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RoomObject)) {
            return false;
        }
        RoomObject ro = (RoomObject) obj;
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
    public String getColorTaggedName() {
        return this.getStartTagName() + this.getName() + this.getEndTagName();
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}
