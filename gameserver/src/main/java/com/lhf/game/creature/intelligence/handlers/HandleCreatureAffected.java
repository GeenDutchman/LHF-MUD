package com.lhf.game.creature.intelligence.handlers;

import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.out.CreatureAffectedMessage;
import com.lhf.messages.out.OutMessage;

public class HandleCreatureAffected extends AIHandler {

    public HandleCreatureAffected() {
        super(OutMessageType.CREATURE_AFFECTED);
    }

    private void handleOtherDeath(BasicAI bai, CreatureAffectedMessage caMessage) {
        if (caMessage.isResultedInDeath()) {
            if (bai.getNpc().getConvoTree() != null) {
                bai.getNpc().getConvoTree().forgetBookmark(caMessage.getAffected());
                bai.getBattleMemories().remove(caMessage.getAffected().getName());
            }
        }
    }

    @Override
    public void handle(BasicAI bai, OutMessage msg) {
        if (OutMessageType.CREATURE_AFFECTED.equals(msg.getOutType())) {
            CreatureAffectedMessage caMessage = (CreatureAffectedMessage) msg;
            bai.updateBattleMemories(caMessage);
            this.handleOtherDeath(bai, caMessage);
        }

    }

}
