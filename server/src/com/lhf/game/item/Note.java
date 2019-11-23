package com.lhf.game.item;

import com.lhf.game.map.objects.sharedinterfaces.Examinable;

public class Note extends Item implements Examinable {

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
