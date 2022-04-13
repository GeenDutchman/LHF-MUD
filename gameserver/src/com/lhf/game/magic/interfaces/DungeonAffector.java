package com.lhf.game.magic.interfaces;

import com.lhf.game.magic.ISpell;
import com.lhf.game.map.Dungeon;

public abstract class DungeonAffector extends ISpell {
    protected Dungeon dungeon;

    protected DungeonAffector(Integer level, String name, String description) {
        super(level, name, description);
        this.dungeon = null;
    }

    public DungeonAffector setDungeon(Dungeon ibaif) {
        this.dungeon = ibaif;
        return this;
    }

    public Dungeon getDungeon() {
        return this.dungeon;
    }

}
