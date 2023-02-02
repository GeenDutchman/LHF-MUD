package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.Objects;

import com.lhf.game.creature.conversation.ConversationBuilder;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.MonsterAI;

public class Monster extends NonPlayerCharacter {
    public static final String defaultConvoTreeName = "non_verbal_default";
    private final long monsterNumber;
    private boolean activelyHostile;

    private MonsterAI aiType;

    public static class MonsterBuilder extends NPCBuilder {
        private boolean activelyHostile;
        private static long serialNumber = 0;
        private long monsterNumber = 0;

        protected MonsterBuilder() {
            super();
            this.setFaction(CreatureFaction.MONSTER);
        }

        public static MonsterBuilder getInstance() {
            return new MonsterBuilder();
        }

        @Override
        public MonsterBuilder useDefaultConversation(ConversationManager convoManager) throws FileNotFoundException {
            if (convoManager != null) {
                this.setConversationTree(convoManager.convoTreeFromFile(Monster.defaultConvoTreeName));
            }
            return this;
        }

        public MonsterBuilder setHostility(boolean activelyHostile) {
            this.activelyHostile = activelyHostile;
            return this;
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

        @Override
        public Monster build() {
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

    @Override
    public void restoreFaction() {
        this.setFaction(CreatureFaction.MONSTER);
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
        Monster monster = (Monster) o;
        return monsterNumber == monster.monsterNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(monsterNumber) + Objects.hash(this.getName()) * 13;
    }

}
