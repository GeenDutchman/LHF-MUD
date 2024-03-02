package com.lhf.game.creature;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.GameEventTester;
import com.lhf.messages.events.QuestEvent.QuestEventType;

public class QuestSource extends CreatureEffectSource {
    public final static String QUEST_PREFIX = "QUEST:";

    public static class Builder extends CreatureEffectSource.AbstractBuilder<Builder> {

        public Builder(String name) {
            super(name != null && !name.startsWith(QUEST_PREFIX) ? QUEST_PREFIX + name : name);
        }

        public Builder setSuccessDeltas(Deltas onSuccess) {
            this.setDeltaForTester(
                    new GameEventTester(GameEventType.QUEST,
                            Set.of(this.getName(), QuestEventType.COMPLETED.toString()),
                            Set.of(QuestEventType.FAILED.toString()), null),
                    onSuccess);
            return getThis();
        }

        public Builder setFailureDeltas(Deltas onFailure) {
            this.setDeltaForTester(
                    new GameEventTester(GameEventType.QUEST, Set.of(this.getName(), QuestEventType.FAILED.toString()),
                            Set.of(QuestEventType.COMPLETED.toString()), null),
                    onFailure);
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

    public static Builder getBuilder(String name) {
        return new Builder(name);
    }

    protected QuestSource(CreatureEffectSource.AbstractBuilder<?> builder) {
        super(builder.getName() != null && !builder.getName().startsWith(QUEST_PREFIX)
                ? builder.setName(QUEST_PREFIX + builder.getName())
                : builder);
    }

    @Override
    public QuestSource makeCopy() {
        QuestSource copy = new QuestSource(this.getName(), persistence, resistance, description, onApplication,
                onTickEvent, onRemoval, null, null);
        return copy;
    }

    @Override
    public boolean isOffensive() {
        return false;
    }

    @Override
    public String printDescription() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("This quest").add(String.format("\"%s\"", this.getName())).add("entails the following:");
        sj.add(this.description).add("\r\n");
        if (this.onApplication != null) {
            final String applicationDescription = this.onApplication.printDescription();
            if (applicationDescription.length() > 0) {
                sj.add("On application:").add(applicationDescription);
            }
        }
        if (this.onTickEvent != null && this.onTickEvent.size() > 0) {
            for (final Entry<GameEventTester, Deltas> tickDeltas : this.onTickEvent.entrySet()) {
                final GameEventTester tester = tickDeltas.getKey();
                final Deltas deltas = tickDeltas.getValue();
                if (tester == null || deltas == null) {
                    continue;
                }
                final String tickDescription = deltas.printDescription();
                if (tickDescription.length() > 0) {
                    sj.add(tester.toString()).add(tickDescription);
                }
            }
        }
        if (this.onRemoval != null) {
            final String removalDescription = this.onRemoval.printDescription();
            if (removalDescription.length() > 0) {
                sj.add("On removal:").add(removalDescription);
            }
        }
        return sj.toString();
    }

}
