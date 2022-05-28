package com.lhf.game.item.concrete;

import com.lhf.game.item.Item;

public class Note extends Item {

    private String noteContent;

    public Note(String name, boolean isVisible, String content) {
        super(name, isVisible);
        noteContent = content;
    }

    @Override
    public String getDescription() {
        return noteContent;
    }

}