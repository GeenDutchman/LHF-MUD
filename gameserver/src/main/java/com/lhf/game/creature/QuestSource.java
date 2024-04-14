package com.lhf.game.creature;

import java.util.Set;
import java.util.StringJoiner;

import com.lhf.messages.GameEventType;
import com.lhf.messages.events.GameEventTester;
import com.lhf.messages.events.QuestEvent.QuestEventType;

public class QuestSource extends CreatureEffectSource {
    public final static String QUEST_PREFIX = "QUEST:";

    public static class Builder extends CreatureEffectSource.AbstractBuilder<Builder> {

        private final static GameEventTester produceSuccessTester(Builder builder) {
            return new GameEventTester(GameEventType.QUEST,
                    Set.of(builder.getName(), QuestEventType.COMPLETED.toString()),
                    Set.of(QuestEventType.FAILED.toString()), null, false);
        }

        private final static GameEventTester produceFailureTester(Builder builder) {
            return new GameEventTester(GameEventType.QUEST, Set.of(builder.getName(), QuestEventType.FAILED.toString()),
                    Set.of(QuestEventType.COMPLETED.toString()), null, false);
        }

        public Builder(String name) {
            super(name != null && !name.startsWith(QUEST_PREFIX) ? QUEST_PREFIX + name : name);
        }

        public Builder setSuccessDeltas(Deltas onSuccess) {
            this.setDeltaForTester(Builder.produceSuccessTester(getThis()), onSuccess);
            return getThis();
        }

        public Builder setFailureDeltas(Deltas onFailure) {
            this.setDeltaForTester(Builder.produceFailureTester(getThis()), onFailure);
            return getThis();
        }

        @Override
        public Builder getThis() {
            return this;
        }

        public QuestSource build() {
            return new QuestSource(getThis());
        }

    }

    public static Builder getQuestBuilder(String name) {
        return new Builder(name);
    }

    protected QuestSource(CreatureEffectSource.AbstractBuilder<?> builder) {
        super(builder.getName() != null && !builder.getName().startsWith(QUEST_PREFIX)
                ? builder.setName(QUEST_PREFIX + builder.getName())
                : builder);
    }

    @Override
    public boolean isOffensive() {
        return false;
    }

    @Override
    public String getTagName() {
        return "Quest";
    }

    @Override
    public String getDescription() {
        StringJoiner sj = new StringJoiner(" ");
        if (this.description != null && !this.description.isEmpty() && !this.description.isBlank()) {
            sj.add("This quest").add("entails the following:");
            sj.add(this.description).add("\r\n");
        }
        return sj.add(super.getDescription()).toString();

    }

}
