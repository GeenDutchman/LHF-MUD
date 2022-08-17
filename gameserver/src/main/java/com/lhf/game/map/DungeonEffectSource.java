package com.lhf.game.map;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EntityEffectSource;

public class DungeonEffectSource extends EntityEffectSource {
    protected boolean addsRoomToDungeon;

    public DungeonEffectSource(String name, EffectPersistence persistence, String description,
            boolean addsRoomToDungeon) {
        super(name, persistence, description);
        this.addsRoomToDungeon = addsRoomToDungeon;
    }

    public boolean isAddsRoomToDungeon() {
        return addsRoomToDungeon;
    }

    public void setAddsRoomToDungeon(boolean addsRoomToDungeon) {
        this.addsRoomToDungeon = addsRoomToDungeon;
    }

    @Override
    public String printDescription() {
        return super.printDescription()
                + (this.addsRoomToDungeon ? "\r\nAdds a room to the dungeon!" : "\r\nWill modify the current dungeon.");
    }

}
