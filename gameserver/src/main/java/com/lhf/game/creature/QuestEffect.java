package com.lhf.game.creature;

import com.lhf.Taggable;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.QuestEvent;
import com.lhf.messages.events.QuestEvent.QuestEventType;

public class QuestEffect extends CreatureEffect {
    protected boolean finished;

    public QuestEffect(QuestSource source, ICreature creatureResponsible, Taggable generatedBy) {
        super(source, creatureResponsible, generatedBy);
        this.finished = false;
    }

    @Override
    public QuestSource getSource() {
        return (QuestSource) this.source;
    }

    @Override
    public boolean tick(GameEvent tickEvent) {
        if (tickEvent == null) {
            return false;
        }
        if (GameEventType.QUEST.equals(tickEvent.getEventType()) && tickEvent instanceof QuestEvent questEvent
                && this.getName().equals(questEvent.getQuestName())) {
            if (QuestEventType.COMPLETED.equals(questEvent.getQuestEventType())
                    || QuestEventType.FAILED.equals(questEvent.getQuestEventType())) {
                this.finished = true;
            }
            return true;
        }
        return super.tick(tickEvent);
    }

    @Override
    public boolean isReadyForRemoval() {
        if (this.finished) {
            return true;
        }
        return super.isReadyForRemoval();
    }

}
