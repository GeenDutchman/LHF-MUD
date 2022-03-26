package com.lhf.game.item;

import com.lhf.game.map.objects.sharedinterfaces.Examinable;
import com.lhf.game.map.objects.sharedinterfaces.Taggable;

public abstract class Item implements Examinable, Taggable {
    // Class name for discrimination
    private final String className;
    // Name it will be known by
    private String objectName;
    // Will not output with look if false
    private boolean isVisible;

    public Item(String name, boolean isVisible) {
        this.className = this.getClass().getName();
        this.objectName = name;
        this.isVisible = isVisible;
    }

    public String getClassName() {
        return this.className;
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
        if (!(obj instanceof Item)) {
            return false;
        }
        Item ro = (Item) obj;
        return objectName.equals(ro.objectName);
    }

    @Override
    public String getStartTagName() {
        return "<item>";
    }

    @Override
    public String getEndTagName() {
        return "</item>";
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTagName() + this.getName() + this.getEndTagName();
    }

}
