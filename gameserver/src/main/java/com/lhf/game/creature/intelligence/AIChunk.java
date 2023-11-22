package com.lhf.game.creature.intelligence;

import com.lhf.game.events.messages.out.OutMessage;

public interface AIChunk {
    public void handle(BasicAI bai, OutMessage msg);
}
