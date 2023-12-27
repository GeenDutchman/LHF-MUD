package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.function.UnaryOperator;

import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.CommandChainHandler;

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
        protected Monster preEnforcedRegistrationBuild(CommandChainHandler successor,
                StatblockManager statblockManager, UnaryOperator<MonsterBuilder> composedlazyLoaders)
                throws FileNotFoundException {
            if (statblockManager != null) {
                this.loadStatblock(statblockManager);
            }
            if (composedlazyLoaders != null) {
                composedlazyLoaders.apply(this.getThis());
            }
            Monster created = new Monster(this);
            created.setSuccessor(successor);
            return created;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) && obj instanceof MonsterBuilder;
        }
    }

    public Monster(MonsterBuilder builder) {
        super(builder);
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
