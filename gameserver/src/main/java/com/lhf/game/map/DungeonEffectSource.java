package com.lhf.game.map;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.EntityEffectSource;

public class DungeonEffectSource extends EntityEffectSource {
    protected final boolean addsRoomToDungeon;

    public DungeonEffectSource(String name, EffectPersistence persistence, EffectResistance resistance,
            String description, boolean addsRoomToDungeon) {
        super(name, persistence, resistance, description);
        this.addsRoomToDungeon = addsRoomToDungeon;
    }

    @Override
    public DungeonEffectSource makeCopy() {
        return new DungeonEffectSource(this.getName(), persistence, resistance, description, addsRoomToDungeon);
    }

    public boolean isAddsRoomToDungeon() {
        return addsRoomToDungeon;
    }

    @Override
    public String printDescription() {
        return super.printDescription()
                + (this.addsRoomToDungeon ? "\r\nAdds a room to the dungeon!" : "\r\nWill modify the current dungeon.");
    }

    @Override
    public boolean isOffensive() {
        return false;
    }

    @Override
    public int aiScore() {
        return 0;
    }

}
