package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.CommandChainHandler;
import com.lhf.server.client.CommandInvoker;

public class Monster extends NonPlayerCharacter implements IMonster {
    private final long monsterNumber;

    public static class MonsterBuilder extends INonPlayerCharacter.AbstractNPCBuilder<MonsterBuilder, Monster> {
        private static transient long serialNumber = 0;
        private transient long monsterNumber = 0;

        private MonsterBuilder() {
            super();
            this.setFaction(CreatureFaction.MONSTER);
        }

        public static MonsterBuilder getInstance() {
            return new MonsterBuilder();
        }

        @Override
        public MonsterBuilder makeCopy() {
            MonsterBuilder monsterBuilder = new MonsterBuilder();
            monsterBuilder.copyFrom(this);
            return monsterBuilder;
        }

        @Override
        protected MonsterBuilder getThis() {
            return this;
        }

        @Override
        public MonsterBuilder useDefaultConversation() {
            this.setConversationFileName(IMonster.defaultConvoTreeName);
            return this.getThis();
        }

        private synchronized void nextSerial() {
            MonsterBuilder.serialNumber++;
            this.monsterNumber = MonsterBuilder.serialNumber;
        }

        public long getMonsterNumber() {
            if (this.monsterNumber == 0) {
                this.nextSerial();
            }
            return this.monsterNumber;
        }

        @Override
        public Monster quickBuild(Supplier<CommandInvoker> controllerSupplier, CommandChainHandler successor) {
            Statblock block = this.getStatblock();
            if (block == null) {
                this.useBlankStatblock();
            }
            return new Monster(this, controllerSupplier, () -> successor, () -> this.getStatblock(),
                    () -> null);
        }

        @Override
        public Monster build(Supplier<CommandInvoker> controllerSupplier,
                CommandChainHandler successor, StatblockManager statblockManager,
                UnaryOperator<MonsterBuilder> composedlazyLoaders) throws FileNotFoundException {
            if (statblockManager != null) {
                this.loadStatblock(statblockManager);
            }
            if (composedlazyLoaders != null) {
                composedlazyLoaders.apply(this.getThis());
            }
            return new Monster(this, controllerSupplier, () -> successor, () -> this.getStatblock(),
                    () -> this.getConversationTree());
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) && obj instanceof MonsterBuilder;
        }
    }

    public Monster(MonsterBuilder builder,
            Supplier<CommandInvoker> controllerSupplier, Supplier<CommandChainHandler> successorSupplier,
            Supplier<Statblock> statblockSupplier, Supplier<ConversationTree> conversationSupplier) {
        super(builder, controllerSupplier, successorSupplier, statblockSupplier, conversationSupplier);
        this.monsterNumber = builder.getMonsterNumber();
    }

    public static MonsterBuilder getMonsterBuilder() {
        return new MonsterBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        IMonster monster = (IMonster) o;
        return monsterNumber == monster.getMonsterNumber();
    }

    @Override
    public int hashCode() {
        return Objects.hash(monsterNumber) + Objects.hash(this.getName()) * 13;
    }

    @Override
    public long getMonsterNumber() {
        return this.monsterNumber;
    }

}
