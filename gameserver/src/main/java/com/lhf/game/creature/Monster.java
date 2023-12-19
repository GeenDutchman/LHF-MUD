package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.Objects;

import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.MonsterAI;

public class Monster extends NonPlayerCharacter implements IMonster {
    private final long monsterNumber;
    private boolean activelyHostile;

    private MonsterAI aiType;

    public static class MonsterBuilder extends INonPlayerCharacter.AbstractNPCBuilder<MonsterBuilder> {
        private boolean activelyHostile;
        private static long serialNumber = 0;
        private long monsterNumber = 0;

        private MonsterBuilder(AIRunner aiRunner) {
            super(aiRunner);
            this.setFaction(CreatureFaction.MONSTER);
        }

        public static MonsterBuilder getInstance(AIRunner aiRunner) {
            return new MonsterBuilder(aiRunner);
        }

        @Override
        protected MonsterBuilder getThis() {
            return this;
        }

        @Override
        public MonsterBuilder useDefaultConversation(ConversationManager convoManager) throws FileNotFoundException {
            if (convoManager != null) {
                this.setConversationTree(convoManager.convoTreeFromFile(IMonster.defaultConvoTreeName));
            }
            return this.getThis();
        }

        public MonsterBuilder setHostility(boolean activelyHostile) {
            this.activelyHostile = activelyHostile;
            return this.getThis();
        }

        public boolean getHostility() {
            return this.activelyHostile;
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

        protected IMonster register(IMonster npc) {
            if (this.getAiRunner() != null) {
                this.getAiRunner().register(npc, this.getAiHandlersAsArray());
            }
            return npc;
        }

        @Override
        public IMonster build() {
            return this.register(this.preEnforcedRegistrationBuild());
        }

        @Override
        protected IMonster preEnforcedRegistrationBuild() {
            this.nextSerial();
            return new Monster(this);
        }
    }

    public Monster(MonsterBuilder builder) {
        super(builder);
        this.activelyHostile = builder.getHostility();
        this.monsterNumber = builder.getMonsterNumber();
        this.aiType = MonsterAI.RANDOM;
        this.setFaction(CreatureFaction.MONSTER);
    }

    public static MonsterBuilder getMonsterBuilder(AIRunner aiRunner) {
        return new MonsterBuilder(aiRunner);
    }

    public void setAiType(MonsterAI newType) {
        this.aiType = newType;
    }

    public void setActivelyHostile(boolean setting) {
        this.activelyHostile = setting;
    }

    public boolean isActivelyHostile() {
        return this.activelyHostile;
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
