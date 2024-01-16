package com.lhf.game.item;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.PatternSyntaxException;

public abstract class Item implements IItem {
    public final static class ItemID implements Comparable<ItemID> {
        private final UUID id = UUID.randomUUID();

        public UUID getId() {
            return id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof ItemID))
                return false;
            ItemID other = (ItemID) obj;
            return Objects.equals(id, other.id);
        }

        @Override
        public String toString() {
            return this.id.toString();
        }

        @Override
        public int compareTo(ItemID arg0) {
            return this.id.compareTo(arg0.id);
        }

    }

    // Class name for discrimination
    private final String className;
    // Name it will be known by
    private String objectName;
    // Will not output with look if false
    private boolean visible;
    // Every item should describe itself
    protected String descriptionString;

    public Item() {
        this.className = this.getClass().getName();
        this.objectName = "Item";
        this.visible = true;
        this.descriptionString = "An Item";
    }

    public Item(String name) {
        assert name.trim().length() >= 3;
        this.className = this.getClass().getName();
        this.objectName = name.trim();
        this.visible = true;
        this.descriptionString = this.objectName;
    }

    public Item(String name, String description) {
        assert name.trim().length() >= 3;
        this.className = this.getClass().getName();
        this.objectName = name.trim();
        this.visible = true;
        this.descriptionString = description;
    }

    protected void copyOverwriteTo(Item other) {
        other.descriptionString = this.descriptionString;
    }

    @Override
    public abstract Item makeCopy();

    @Override
    public abstract void acceptItemVisitor(ItemVisitor visitor);

    @Override
    public String getClassName() {
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
        return objectName.equals(ro.objectName);
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
