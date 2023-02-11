package com.lhf.game.creature;

import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.vocation.DMV;

public class DungeonMaster extends NonPlayerCharacter {

    public static class DungeonMasterBuilder extends NonPlayerCharacter.AbstractNPCBuilder<DungeonMasterBuilder> {
        private DungeonMasterBuilder(AIRunner aiRunner) {
            super(aiRunner);
            this.setVocation(new DMV());
        }

        protected DungeonMaster register(DungeonMaster npc) {
            if (this.getAiRunner() != null) {
                this.getAiRunner().register(npc, this.getAiHandlersAsArray());
            }
            return npc;
        }

        public static DungeonMasterBuilder getInstance(AIRunner aiRunner) {
            return new DungeonMasterBuilder(aiRunner);
        }

        public DungeonMaster build() {
            return this.register(new DungeonMaster(this));
        }
    }

    public DungeonMaster(DungeonMasterBuilder builder) {
        super(builder);
    }

    public static DungeonMasterBuilder getDMBuilder(AIRunner aiRunner) {
        return new DungeonMasterBuilder(aiRunner);
    }

}
