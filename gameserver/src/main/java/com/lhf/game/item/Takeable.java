package com.lhf.game.item;

public class Takeable extends Item {

    public Takeable(String name) {
        super(name);
    }

    public Takeable(String name, String description) {
        super(name, description);
    }

    protected void copyOverwriteTo(Takeable other) {
        super.copyOverwriteTo(other);
    }

    @Override
    public Takeable makeCopy() {
        Takeable takeable = new Takeable(this.getName(), descriptionString);
        this.copyOverwriteTo(takeable);
        return takeable;
    }

    @Override
    public void acceptItemVisitor(ItemVisitor visitor) {
        visitor.visit(this);
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
