package com.lhf.game.item;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.PatternSyntaxException;

import com.lhf.TaggedExaminable;
import com.lhf.messages.events.SeeEvent;

public abstract class Item implements TaggedExaminable {
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

    public Item(String name, boolean isVisible) {
        this.className = this.getClass().getName();
        this.objectName = name.trim();
        assert this.objectName.length() >= 3;
        this.visible = isVisible;
        this.descriptionString = this.getColorTaggedName();
    }

    public Item(String name, boolean isVisible, String description) {
        this.className = this.getClass().getName();
        this.objectName = name.trim();
        assert this.objectName.length() >= 3;
        this.visible = isVisible;
        this.descriptionString = description;
    }

    protected void copyOverwriteTo(Item other) {
        other.descriptionString = this.descriptionString;
    }

    public abstract Item makeCopy();

    public abstract void acceptItemVisitor(ItemVisitor visitor);

    public String getClassName() {
        return this.className;
    }

    public boolean isVisible() {
        return visible;
    }

    protected void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getName() {
        return objectName;
    }

    public boolean checkName(String name) {
        return this.getName().equalsIgnoreCase(name.trim());
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

    @Override
    public SeeEvent produceMessage() {
        SeeEvent.Builder seeOutMessage = SeeEvent.getBuilder().setExaminable(this);
        return seeOutMessage.Build();
    }

}
