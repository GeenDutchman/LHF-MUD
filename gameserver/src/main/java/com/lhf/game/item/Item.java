package com.lhf.game.item;

import java.util.regex.PatternSyntaxException;

import com.lhf.TaggedExaminable;
import com.lhf.messages.out.SeeOutMessage;

public abstract class Item implements TaggedExaminable {
    // Class name for discrimination
    private final String className;
    // Name it will be known by
    private String objectName;
    // Will not output with look if false
    private boolean isVisible;
    // Every item should describe itself
    protected String descriptionString;

    public Item(String name, boolean isVisible) {
        this.className = this.getClass().getName();
        this.objectName = name.trim();
        assert this.objectName.length() >= 3;
        this.isVisible = isVisible;
        this.descriptionString = this.getColorTaggedName();
    }

    public Item(String name, boolean isVisible, String description) {
        this.className = this.getClass().getName();
        this.objectName = name.trim();
        assert this.objectName.length() >= 3;
        this.isVisible = isVisible;
        this.descriptionString = description;
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
    public SeeOutMessage produceMessage() {
        SeeOutMessage.Builder seeOutMessage = SeeOutMessage.getBuilder().setExaminable(this);
        return seeOutMessage.Build();
    }

}
