package com.lhf.game.item.concrete;

import com.lhf.game.item.Item;
import com.lhf.game.item.ItemVisitor;

public class Note extends Item {

    private String noteContent;

    public Note(String name, boolean isVisible, String content) {
        super(name, isVisible);
        noteContent = content;
    }

    @Override
    public void acceptVisitor(ItemVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Note makeCopy() {
        return new Note(this.getName(), this.checkVisibility(), this.noteContent);
    }

    @Override
    public String printDescription() {
        return noteContent;
    }

}
