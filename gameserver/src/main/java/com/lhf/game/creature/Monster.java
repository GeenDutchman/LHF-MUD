package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.function.UnaryOperator;

import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.CommandChainHandler;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.interfaces.NotNull;

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
        public Monster quickBuild(CommandInvoker controller, CommandChainHandler successor) {
            Statblock block = this.getStatblock();
            if (block == null) {
                this.useBlankStatblock();
            }
            return Monster.buildMonster(this, controller, successor, this.getStatblock(),
                    null, null);
        }

        @Override
        public Monster build(CommandInvoker controller,
                CommandChainHandler successor, StatblockManager statblockManager,
                UnaryOperator<MonsterBuilder> composedlazyLoaders) throws FileNotFoundException {
            if (statblockManager != null) {
                this.loadStatblock(statblockManager);
            }
            if (composedlazyLoaders != null) {
                composedlazyLoaders.apply(this.getThis());
            }
            return Monster.buildMonster(this, controller, successor, this.getStatblock(),
                    this.getConversationTree(), null);
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) && obj instanceof MonsterBuilder;
        }
    }

    public static Monster buildMonster(MonsterBuilder builder,
            CommandInvoker controller, CommandChainHandler successor,
            Statblock statblock, ConversationTree converstionTree,
            UnaryOperator<Monster> transformer) {
        Monster made = new Monster(builder, controller, successor, statblock,
                converstionTree);
        if (transformer != null) {
            made = transformer.apply(made);
        }
        return made;
    }

    protected Monster(MonsterBuilder builder,
            @NotNull CommandInvoker controller, CommandChainHandler successor,
            @NotNull Statblock statblock, ConversationTree conversationTree) {
        super(builder, controller, successor, statblock, conversationTree);
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
