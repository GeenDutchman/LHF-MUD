package com.lhf.game.creature.intelligence;

import com.lhf.messages.out.OutMessage;

public interface AIChunk {
    public void handle(BasicAI bai, OutMessage msg);
}
