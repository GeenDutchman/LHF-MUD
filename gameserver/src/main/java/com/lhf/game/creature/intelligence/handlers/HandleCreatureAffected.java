package com.lhf.game.creature.intelligence.handlers;

import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.CreatureAffectedEvent;
import com.lhf.messages.events.GameEvent;

public class HandleCreatureAffected extends AIHandler {

    public HandleCreatureAffected() {
        super(GameEventType.CREATURE_AFFECTED);
    }

    private void handleOuch(BasicAI bai, CreatureAffectedEvent caMessage) {
        if (bai.getNpc().isInBattle()) {
            if (caMessage.getAffected() == bai.getNpc() && caMessage.getEffect().isOffensive()) {
                bai.getNpc().getHarmMemories().update(caMessage);
            }
        }
    }

    private void handleOtherDeath(BasicAI bai, CreatureAffectedEvent caMessage) {
        if (caMessage.isResultedInDeath()) {
            if (bai.getNpc().getConvoTree() != null) {
                bai.getNpc().getConvoTree().forgetBookmark(caMessage.getAffected());
            }
        }
    }

    @Override
    public void handle(BasicAI bai, GameEvent event) {
        if (GameEventType.CREATURE_AFFECTED.equals(event.getEventType())) {
            CreatureAffectedEvent caMessage = (CreatureAffectedEvent) event;
            this.handleOtherDeath(bai, caMessage);
            this.handleOuch(bai, caMessage);
        }

    }

}
