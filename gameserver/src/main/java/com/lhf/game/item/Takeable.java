package com.lhf.game.item;

public class Takeable extends Item {

    public Takeable(String name, boolean isVisible) {
        super(name, isVisible);
    }

    public Takeable(String name, boolean isVisible, String description) {
        super(name, isVisible, description);
    }

    @Override
    public String getStartTag() {
        return "<takeable>";
    }

    @Override
    public String getEndTag() {
        return "</takeable>";
    }

}
