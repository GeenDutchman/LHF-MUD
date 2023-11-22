package com.lhf.game.creature.intelligence.handlers;

import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.game.events.messages.OutMessageType;
import com.lhf.game.events.messages.out.CreatureAffectedMessage;
import com.lhf.game.events.messages.out.OutMessage;

public class HandleCreatureAffected extends AIHandler {

    public HandleCreatureAffected() {
        super(OutMessageType.CREATURE_AFFECTED);
    }

    private void handleOuch(BasicAI bai, CreatureAffectedMessage caMessage) {
        if (bai.getNpc().isInBattle()) {
            if (caMessage.getAffected() == bai.getNpc() && caMessage.getEffect().isOffensive()) {
                bai.getNpc().getHarmMemories().update(caMessage);
            }
        }
    }

    private void handleOtherDeath(BasicAI bai, CreatureAffectedMessage caMessage) {
        if (caMessage.isResultedInDeath()) {
            if (bai.getNpc().getConvoTree() != null) {
                bai.getNpc().getConvoTree().forgetBookmark(caMessage.getAffected());
            }
        }
    }

    @Override
    public void handle(BasicAI bai, OutMessage msg) {
        if (OutMessageType.CREATURE_AFFECTED.equals(msg.getOutType())) {
            CreatureAffectedMessage caMessage = (CreatureAffectedMessage) msg;
            this.handleOtherDeath(bai, caMessage);
            this.handleOuch(bai, caMessage);
        }

    }

}
