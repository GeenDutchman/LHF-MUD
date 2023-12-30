package com.lhf.game.item;

public class Takeable extends Item {

    public Takeable(String name, boolean isVisible) {
        super(name, isVisible);
    }

    public Takeable(String name, boolean isVisible, String description) {
        super(name, isVisible, description);
    }

    protected void copyOverwriteTo(Takeable other) {
        super.copyOverwriteTo(other);
    }

    @Override
    public Takeable makeCopy() {
        Takeable takeable = new Takeable(this.getName(), this.checkVisibility(), descriptionString);
        this.copyOverwriteTo(takeable);
        return takeable;
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
