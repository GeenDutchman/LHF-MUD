package com.lhf.game.creature;

import com.lhf.game.creature.vocation.DMV;

public class DungeonMaster extends NonPlayerCharacter {

    CreatureCreator creatureCreator;

    public DungeonMaster(String name) {
        super(name, new DMV());
    }

}
