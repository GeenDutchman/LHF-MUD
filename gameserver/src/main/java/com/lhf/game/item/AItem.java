package com.lhf.game.item;

import java.util.regex.PatternSyntaxException;

public abstract class AItem implements IItem {

    private final ItemID itemID;
    // Class name for discrimination
    private final String className;
    // Name it will be known by
    private final String objectName;
    // Will not output with look if false
    private boolean visible;
    // Every item should describe itself
    protected String descriptionString;

    public AItem(String name) {
        assert name.trim().length() >= 3;
        this.itemID = new ItemID();
        this.className = this.getClass().getName();
        this.objectName = name.trim();
        this.visible = true;
        this.descriptionString = this.objectName;
    }

    public AItem(String name, String description) {
        assert name.trim().length() >= 3;
        this.itemID = new ItemID();
        this.className = this.getClass().getName();
        this.objectName = name.trim();
        this.visible = true;
        this.descriptionString = description;
    }

    protected AItem(ItemID itemID, String className, String name, boolean visible, String descriptionString) {
        if (itemID == null) {
            throw new IllegalArgumentException("item id cannot be null");
        } else if (name == null || name.trim().length() < 3) {
            throw new IllegalArgumentException(
                    String.format("name argument cannot be null or have a length shorter than 3: %s", name));
        } else if (className == null) {
            throw new IllegalArgumentException("object name is critical and cannot be null!");
        }
        this.itemID = itemID;
        this.objectName = name;
        this.className = className;
        this.visible = visible;
        this.descriptionString = descriptionString;
    }

    @Override
    public ItemID getItemID() {
        return this.itemID;
    }

    @Override
    public abstract AItem makeCopy();

    @Override
    public abstract void acceptItemVisitor(ItemVisitor visitor);

    @Override
    public final String getClassName() {
        return this.className;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    protected void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public String getName() {
        return objectName;
    }

    @Override
    public boolean checkName(String name) {
        return this.getName().equalsIgnoreCase(name.trim());
    }

    @Override
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
        if (!(obj instanceof AItem)) {
            return false;
        }
        AItem ro = (AItem) obj;
        if (objectName.equals(ro.objectName)) {
            return this.itemID.equals(ro.itemID);
        }
        return false;
    }

    @Override
    public String getStartTag() {
        return "<item>";
    }

    @Override
    public String getEndTag() {
        return "</item>";
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTag() + this.getName() + this.getEndTag();
    }

    @Override
    public String printDescription() {
        return this.descriptionString;
    }

}
