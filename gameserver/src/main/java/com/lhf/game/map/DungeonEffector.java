package com.lhf.game.map;

import com.lhf.game.EntityEffect;

public interface DungeonEffector extends EntityEffect {
    public String getRoomName();

    public String getRoomDescription();

    public Directions getDirectionToAddedRoom();

    public Room getRoomToAdd();

}
