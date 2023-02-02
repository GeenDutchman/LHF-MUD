package com.lhf.game.creature;

import com.lhf.game.creature.vocation.DMV;

public class DungeonMaster extends NonPlayerCharacter {

    public static class DungeonMasterBuilder extends NonPlayerCharacter.NPCBuilder {
        private DungeonMasterBuilder() {
            super();
            this.setVocation(new DMV());
        }

        public static DungeonMasterBuilder getInstance() {
            return new DungeonMasterBuilder();
        }
    }

    public DungeonMaster(DungeonMasterBuilder builder) {
        super(builder);
    }

}
