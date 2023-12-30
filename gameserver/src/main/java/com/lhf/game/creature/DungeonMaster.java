package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.creature.vocation.DMVocation;
import com.lhf.messages.CommandChainHandler;
import com.lhf.server.client.CommandInvoker;

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
        public DungeonMasterBuilder makeCopy() {
            DungeonMasterBuilder builder = new DungeonMasterBuilder();
            builder.copyFrom(this);
            return builder;
        }

        @Override
        public DungeonMaster quickBuild(Supplier<CommandInvoker> controllerSupplier, CommandChainHandler successor) {
            Statblock block = this.getStatblock();
            if (block == null) {
                this.useBlankStatblock();
            }
            return new DungeonMaster(this, controllerSupplier, () -> successor, () -> this.getStatblock(),
                    () -> null);
        }

        @Override
        public DungeonMaster build(Supplier<CommandInvoker> controllerSupplier,
                CommandChainHandler successor, StatblockManager statblockManager,
                UnaryOperator<DungeonMasterBuilder> composedlazyLoaders) throws FileNotFoundException {
            if (statblockManager != null) {
                this.loadStatblock(statblockManager);
            }
            if (composedlazyLoaders != null) {
                composedlazyLoaders.apply(this.getThis());
            }
            return new DungeonMaster(this, controllerSupplier, () -> successor, () -> this.getStatblock(),
                    () -> this.getConversationTree());
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) && obj instanceof DungeonMasterBuilder;
        }
    }

    public DungeonMaster(DungeonMasterBuilder builder,
            Supplier<CommandInvoker> controllerSupplier, Supplier<CommandChainHandler> successorSupplier,
            Supplier<Statblock> statblockSupplier, Supplier<ConversationTree> conversationSupplier) {
        super(builder, controllerSupplier, successorSupplier, statblockSupplier, conversationSupplier);
    }

    public static DungeonMasterBuilder getDMBuilder() {
        return new DungeonMasterBuilder();
    }

}
