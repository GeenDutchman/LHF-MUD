package com.lhf.game.creature.intelligence.handlers;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.creature.conversation.ConversationTreeNodeResult;
import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.messages.CommandBuilder;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.in.SayMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.SpeakingMessage;

public class SpeakingHandler extends AIHandler {

    public SpeakingHandler() {
        super(OutMessageType.SPEAKING);
    }

    @Override
    public void handle(BasicAI bai, OutMessage msg) {
        if (msg.getOutType().equals(this.getOutMessageType())) {
            SpeakingMessage sm = (SpeakingMessage) msg;
            if (!sm.getShouting() && sm.getHearer() != null && sm.getHearer() instanceof NonPlayerCharacter) {
                if (sm.getSayer() instanceof Creature && bai.getNpc().getConvoTree() != null) {
                    Creature sayer = (Creature) sm.getSayer();
                    ConversationTreeNodeResult result = bai.getNpc().getConvoTree().listen(sayer, sm.getMessage());
                    if (result != null && result.getBody() != null) {
                        SayMessage say = (SayMessage) CommandBuilder.fromCommand(CommandMessage.SAY,
                                "say \"" + result.getBody() + "\" to " + sayer.getName());
                        CommandBuilder.addDirect(say, result.getBody());
                        CommandBuilder.addIndirect(say, "to", sayer.getName());
                        bai.handleMessage(null, say);
                    }
                }
            }
        }
    }

}
