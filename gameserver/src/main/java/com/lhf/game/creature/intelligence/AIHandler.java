package com.lhf.game.creature.intelligence;

import java.util.Objects;

import com.lhf.messages.OutMessageType;
import com.lhf.server.interfaces.NotNull;

public abstract class AIHandler {
    protected final OutMessageType outMessageType;
    protected final AIChunk aiChunk;

    public AIHandler(@NotNull OutMessageType outMessageType, @NotNull AIChunk aiChunk) {
        this.outMessageType = outMessageType;
        this.aiChunk = aiChunk;
    }

    public OutMessageType getOutMessageType() {
        return outMessageType;
    }

    public AIChunk getAiChunk() {
        return aiChunk;
    }

    @Override
    public int hashCode() {
        return Objects.hash(outMessageType);
    }

}
