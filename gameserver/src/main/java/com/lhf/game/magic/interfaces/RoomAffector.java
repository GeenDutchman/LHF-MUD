package com.lhf.game.magic.interfaces;

import com.lhf.game.magic.ISpell;
import com.lhf.game.map.Room;

public abstract class RoomAffector extends ISpell {
    protected Room targetRoom;

    protected RoomAffector(Integer level, String name, String description) {
        super(level, name, description);
        this.targetRoom = null;
    }

    public RoomAffector setRoom(Room theRoom) {
        this.targetRoom = theRoom;
        return this;
    }

    public Room getRoom() {
        return this.targetRoom;
    }

}
