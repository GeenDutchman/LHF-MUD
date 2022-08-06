package com.lhf.game.map;

import com.lhf.game.EntityEffector;

public interface DungeonEffector extends EntityEffector {
    public String getRoomName();

    public String getRoomDescription();

    public Directions getDirectionToAddedRoom();

    public Room getRoomToAdd();

}
