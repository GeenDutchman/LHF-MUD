package com.lhf.game.item;

public class Takeable extends AItem {

    public Takeable(String name) {
        super(name);
    }

    public Takeable(String name, String description) {
        super(name, description);
    }

    @Override
    public Takeable makeCopy() {
        return this;
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
