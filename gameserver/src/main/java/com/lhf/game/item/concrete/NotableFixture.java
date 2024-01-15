package com.lhf.game.item.concrete;

import com.lhf.game.item.Item;
import com.lhf.game.item.ItemVisitor;

public class NotableFixture extends Item {

    private String noteContent;

    public NotableFixture(String name, boolean isVisible, String content) {
        super(name, isVisible);
        noteContent = content;
    }

    @Override
    public void acceptItemVisitor(ItemVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NotableFixture makeCopy() {
        return new NotableFixture(this.getName(), this.isVisible(), this.noteContent);
    }

    @Override
    public String printDescription() {
        return noteContent;
    }

}
