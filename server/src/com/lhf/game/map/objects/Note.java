package com.lhf.game.map.objects;

import com.lhf.game.map.objects.interfaces.Examinable;
import com.lhf.game.map.objects.interfaces.Obtainable;

public class Note extends RoomObject implements Examinable, Obtainable {

    private String noteContent;

    public Note(String name, boolean isVisible, String content) {
        super(name, isVisible);
        noteContent = content;
    }

    @Override
    public String getDescription() {
        return noteContent;
    }

    @Override
    public boolean pickUp() {
        return false;
    }
}
