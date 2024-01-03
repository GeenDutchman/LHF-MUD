package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.function.UnaryOperator;

import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.creature.vocation.DMVocation;
import com.lhf.messages.CommandChainHandler;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.interfaces.NotNull;

public class DungeonMaster extends NonPlayerCharacter {

    public static class DungeonMasterBuilder
            extends INonPlayerCharacter.AbstractNPCBuilder<DungeonMasterBuilder, DungeonMaster> {
        private DungeonMasterBuilder() {
            super();
            this.setVocation(new DMVocation());
        }

        @Override
        protected DungeonMasterBuilder getThis() {
            return this;
        }

        public static DungeonMasterBuilder getInstance() {
            return new DungeonMasterBuilder();
        }

        @Override
        public DungeonMasterBuilder makeCopy() {
            DungeonMasterBuilder builder = new DungeonMasterBuilder();
            builder.copyFrom(this);
            return builder;
        }

        @Override
        public DungeonMaster quickBuild(BasicAI basicAI, CommandChainHandler successor) {
            Statblock block = this.getStatblock();
            if (block == null) {
                this.useBlankStatblock();
            }
            if (basicAI == null) {
                basicAI = INonPlayerCharacter.defaultAIRunner.produceAI(getAiHandlersAsArray());
            }
            DungeonMaster dm = new DungeonMaster(this, basicAI,
                    successor, this.getStatblock(),
                    null);
            basicAI.setNPC(dm);
            return dm;
        }

        @Override
        public DungeonMaster build(CommandInvoker controller,
                CommandChainHandler successor, StatblockManager statblockManager,
                UnaryOperator<DungeonMasterBuilder> composedlazyLoaders) throws FileNotFoundException {
            if (statblockManager != null) {
                this.loadStatblock(statblockManager);
            }
            if (composedlazyLoaders != null) {
                composedlazyLoaders.apply(this.getThis());
            }
            return new DungeonMaster(this, controller, successor, this.getStatblock(),
                    this.getConversationTree());
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) && obj instanceof DungeonMasterBuilder;
        }
    }

    public DungeonMaster(DungeonMasterBuilder builder,
            @NotNull CommandInvoker controller, CommandChainHandler successor,
            @NotNull Statblock statblock, ConversationTree conversationTree) {
        super(builder, controller, successor, statblock, conversationTree);
    }

    public static DungeonMasterBuilder getDMBuilder() {
        return new DungeonMasterBuilder();
    }

}
