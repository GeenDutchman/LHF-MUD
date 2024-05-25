package com.lhf.messages.events;

import com.lhf.RichOutput.RichOutputBuilder;
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
                this.setQuestName(quest.getName()).setQuestDescription(quest.getDescription());
            }
            return this;
        }

        public Builder fromQuest(QuestEffect quest) {
            if (quest != null) {
                this.setQuestName(quest.getName()).setQuestDescription(quest.getDescription());
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
    public void buildOutput(RichOutputBuilder builder) {
        if (builder == null) {
            return;
        }
        this.addressCreature(builder, whoseQuest);
        if (this.questEventType == null) {
            builder.appendString(QuestEventType.VIEWED.toString());
        } else {
            builder.appendString(this.questEventType.toString());
        }
        if (this.questDescription != null && !this.isBroadcast()) {
            builder.appendString("a quest described by:").appendString(questDescription);
        } else if (this.questName != null) {
            builder.appendString("a quest named").appendString(questName);
        } else {
            builder.appendString("a quest");
        }
    }

}
