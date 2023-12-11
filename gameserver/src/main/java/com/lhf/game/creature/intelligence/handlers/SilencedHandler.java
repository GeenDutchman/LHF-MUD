package com.lhf.game.creature.intelligence.handlers;

import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.out.OutMessage;
import com.lhf.server.interfaces.NotNull;

public class SilencedHandler extends AIHandler {

    public SilencedHandler(@NotNull OutMessageType outMessageType) {
        super(outMessageType);
    }

    @Override
    public void handle(BasicAI bai, OutMessage msg) {
        // Does nothing, is silenced for logging purposes
    }

}
