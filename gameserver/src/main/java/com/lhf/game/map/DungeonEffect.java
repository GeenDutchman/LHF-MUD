package com.lhf.game.map;

import com.lhf.game.EntityEffect;

public interface DungeonEffect extends EntityEffect {
    public String getRoomName();

    public String getRoomDescription();

    public Directions getDirectionToAddedRoom();

    public Room getRoomToAdd();

}
