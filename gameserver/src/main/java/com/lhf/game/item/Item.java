package com.lhf.game.item;

import java.util.Collection;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class Item implements IItem {

    private final ItemID itemID;
    // Class name for discrimination
    private final String className;
    // Name it will be known by
    private final String objectName;
    // Will not output with look if false
    private boolean visible;
    // Every item should describe itself
    protected String descriptionString;

    public static class ItemBuilder implements IItem.IItemBuilder {
        private String name = "item";
        private String descriptionString = "An item.";
        private boolean visible = true;
        private boolean takeable;
        private String customTagName;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescriptionString() {
            return descriptionString;
        }

        public void setDescriptionString(String descriptionString) {
            this.descriptionString = descriptionString;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public boolean isTakeable() {
            return takeable;
        }

        public void setTakeable(boolean takeable) {
            this.takeable = takeable;
        }

        @Override
        public String getTagName() {
            if (customTagName != null && !customTagName.isBlank()) {
                return customTagName;
            }
            return "item";
        }

        @Override
        public Collection<ItemCapability> getCapabilities() {
            return List.of();
        }

    }

    public Item(String name) {
        assert name.trim().length() >= 3;
        this.itemID = new ItemID();
        this.className = this.getClass().getName();
        this.objectName = name.trim();
        this.visible = true;
        this.descriptionString = this.objectName;
    }

    public Item(String name, String description) {
        assert name.trim().length() >= 3;
        this.itemID = new ItemID();
        this.className = this.getClass().getName();
        this.objectName = name.trim();
        this.visible = true;
        this.descriptionString = description;
    }

    protected Item(ItemID itemID, String className, String name, boolean visible, String descriptionString) {
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
    public Item makeCopy() {
        return new Item(new ItemID(), className, className, visible, descriptionString);
    }

    @Override
    public void acceptItemVisitor(ItemVisitor visitor) {
        if (visitor != null) {
            visitor.visit(this);
        }
    }

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
        if (!(obj instanceof Item)) {
            return false;
        }
        Item ro = (Item) obj;
        if (objectName.equals(ro.objectName)) {
            return this.itemID.equals(ro.itemID);
        }
        return false;
    }

    @Override
    public String getTagName() {
        return "item";
    }

    @Override
    public String getDescription() {
        return this.descriptionString;
    }

}
