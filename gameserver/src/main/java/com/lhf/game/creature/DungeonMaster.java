package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.function.UnaryOperator;

import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.creature.vocation.DMVocation;
import com.lhf.messages.CommandChainHandler;

public class DungeonMaster extends NonPlayerCharacter {

    public static class DungeonMasterBuilder
            extends INonPlayerCharacter.AbstractNPCBuilder<DungeonMasterBuilder, DungeonMaster> {
        private DungeonMasterBuilder() {
            super();
            this.setVocation(new DMVocation());
        }

        public static DungeonMasterBuilder getInstance() {
            return new DungeonMasterBuilder();
        }

        @Override
        protected DungeonMaster preEnforcedRegistrationBuild(CommandChainHandler successor,
                StatblockManager statblockManager, UnaryOperator<DungeonMasterBuilder> composedlazyLoaders)
                throws FileNotFoundException {
            if (statblockManager != null) {
                this.loadStatblock(statblockManager);
            }
            if (composedlazyLoaders != null) {
                composedlazyLoaders.apply(this.getThis());
            }
            DungeonMaster created = new DungeonMaster(this);
            created.setSuccessor(successor);
            return created;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) && obj instanceof DungeonMasterBuilder;
        }
    }

    public DungeonMaster(DungeonMasterBuilder builder) {
        super(builder);
    }

    public static DungeonMasterBuilder getDMBuilder() {
        return new DungeonMasterBuilder();
    }

}
