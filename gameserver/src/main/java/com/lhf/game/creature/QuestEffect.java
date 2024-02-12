package com.lhf.game.creature;

import com.lhf.Taggable;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.QuestEvent;
import com.lhf.messages.events.QuestEvent.QuestEventType;

public class QuestEffect extends CreatureEffect {
    protected MultiRollResult successDamageResult, failureDamageResult;

    public QuestEffect(QuestSource source, ICreature creatureResponsible, Taggable generatedBy) {
        super(source, creatureResponsible, generatedBy);
        this.successDamageResult = null;
        this.failureDamageResult = null;
    }

    @Override
    public QuestSource getSource() {
        return (QuestSource) this.source;
    }

    public void updateSuccessDamageResult(MultiRollResult mrr) {
        this.successDamageResult = mrr;
    }

    public void updateFailureDamageResult(MultiRollResult mrr) {
        this.failureDamageResult = mrr;
    }

    public MultiRollResult getSuccessDamageResult() {
        if (successDamageResult == null) {
            final Deltas deltas = this.getSource().getOnSuccess();
            if (deltas != null) {
                this.successDamageResult = deltas.rollDamages();
            }
        }
        return successDamageResult;
    }

    public MultiRollResult getFailureDamageResult() {
        if (failureDamageResult == null) {
            final Deltas deltas = this.getSource().getOnFailure();
            if (deltas != null) {
                this.failureDamageResult = deltas.rollDamages();
            }
        }
        return failureDamageResult;
    }

    public Deltas getSuccessDeltas() {
        return this.getSource().getOnSuccess();
    }

    public Deltas getFailureDeltas() {
        return this.getSource().getOnFailure();
    }

    @Override
    public boolean tick(GameEvent tickEvent) {
        if (tickEvent == null) {
            return false;
        }
        if (GameEventType.QUEST.equals(tickEvent.getEventType()) && tickEvent instanceof QuestEvent questEvent
                && this.getName().equals(questEvent.getQuestName())) {
            return true;
        }
        return super.tick(tickEvent);
    }

    @Override
    public Deltas getDeltasForEvent(GameEvent event) {
        if (event != null && GameEventType.QUEST.equals(event.getEventType()) && event instanceof QuestEvent questEvent
                && this.getName().equals(questEvent.getQuestName())) {
            if (QuestEventType.COMPLETED.equals(questEvent.getQuestEventType())) {
                return this.getSuccessDeltas();
            } else if (QuestEventType.FAILED.equals(questEvent.getQuestEventType())) {
                return this.getFailureDeltas();
            }
        }
        return super.getDeltasForEvent(event);
    }

    @Override
    public boolean isReadyForRemoval() {
        if (this.successDamageResult != null || this.failureDamageResult != null) {
            return true;
        }
        return super.isReadyForRemoval();
    }

}
