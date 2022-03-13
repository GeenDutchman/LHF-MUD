package com.lhf.game.item;

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
