package com.lhf.game.creature.intelligence;

import com.lhf.messages.out.GameEvent;

public interface AIChunk {
    public void handle(BasicAI bai, GameEvent msg);
}
