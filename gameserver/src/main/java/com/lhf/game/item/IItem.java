package com.lhf.game.item;

import java.util.regex.PatternSyntaxException;

import com.lhf.TaggedExaminable;

public interface IItem extends TaggedExaminable {

    IItem makeCopy();

    void acceptItemVisitor(ItemVisitor visitor);

    String getClassName();

    boolean isVisible();

    @Override
    String getName();

    default boolean checkName(String name) {
        return this.getName().equalsIgnoreCase(name);
    }

    default boolean CheckNameRegex(String possName, Integer minimumLength) {
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
    default String getStartTag() {
        return "<item>";
    }

    @Override
    default String getEndTag() {
        return "</item>";
    }

    @Override
    default String getColorTaggedName() {
        return this.getStartTag() + this.getName() + this.getEndTag();
    }

}