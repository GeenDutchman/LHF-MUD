package com.lhf.game.creature.intelligence;

import com.lhf.messages.events.GameEvent;

public interface AIChunk {
    public void handle(BasicAI bai, GameEvent msg);
}
