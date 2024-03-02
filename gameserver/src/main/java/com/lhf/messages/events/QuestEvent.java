package com.lhf.messages.events;

import java.util.StringJoiner;

import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.QuestEffect;
import com.lhf.game.creature.QuestSource;
import com.lhf.messages.GameEventType;

public class QuestEvent extends GameEvent {
    public enum QuestEventType {
        VIEWED, ACCEPTED, FAILED, COMPLETED;
    }

    private final QuestEventType questEventType;
    private final String questName;
    private final String questDescription;
    private final ICreature whoseQuest;

    public static class Builder extends GameEvent.Builder<Builder> {
        private QuestEventType questEventType;
        private String questName;
        private String questDescription;
        private ICreature whoseQuest;

        protected Builder() {
            super(GameEventType.QUEST);
        }

        public QuestEventType getQuestEventType() {
            return questEventType != null ? questEventType : QuestEventType.VIEWED;
        }

        public Builder setQuestEventType(QuestEventType questEventType) {
            this.questEventType = questEventType;
            return this;
        }

        public String getQuestName() {
            return questName;
        }

        public Builder setQuestName(String questName) {
            this.questName = questName;
            return this;
        }

        public String getQuestDescription() {
            return questDescription;
        }

        public Builder setQuestDescription(String questDescription) {
            this.questDescription = questDescription;
            return this;
        }

        public ICreature getWhoseQuest() {
            return whoseQuest;
        }

        public Builder setWhoseQuest(ICreature whoseQuest) {
            this.whoseQuest = whoseQuest;
            return this;
        }

        public Builder fromQuest(QuestSource quest) {
            if (quest != null) {
                this.setQuestName(quest.getName()).setQuestDescription(quest.printDescription());
            }
            return this;
        }

        public Builder fromQuest(QuestEffect quest) {
            if (quest != null) {
                this.setQuestName(quest.getName()).setQuestDescription(quest.printDescription());
            }
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public QuestEvent Build() {
            return new QuestEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public QuestEvent(Builder builder) {
        super(builder);
        this.questEventType = builder.getQuestEventType();
        this.questDescription = builder.getQuestDescription();
        this.questName = builder.getQuestName();
        this.whoseQuest = builder.getWhoseQuest();
    }

    public QuestEventType getQuestEventType() {
        return questEventType;
    }

    public String getQuestName() {
        return questName;
    }

    public String getQuestDescription() {
        return questDescription;
    }

    public ICreature getWhoseQuest() {
        return whoseQuest;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(this.addressCreature(this.whoseQuest, isBroadcast()));
        if (this.questEventType == null) {
            sj.add(QuestEventType.VIEWED.toString());
        } else {
            sj.add(this.questEventType.toString());
        }
        if (this.questDescription != null && !this.isBroadcast()) {
            sj.add("a quest described by: ").add(this.questDescription);
        } else if (this.questName != null) {
            sj.add("a quest named").add(this.questName);
        } else {
            sj.add("a quest");
        }
        return sj.toString();
    }

    @Override
    public String print() {
        return this.toString();
    }

}
