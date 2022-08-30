package com.lhf.game.creature.intelligence;

import java.util.Objects;

import com.lhf.messages.OutMessageType;
import com.lhf.server.interfaces.NotNull;

public abstract class AIHandler implements AIChunk {
    protected final OutMessageType outMessageType;

    public AIHandler(@NotNull OutMessageType outMessageType) {
        this.outMessageType = outMessageType;
    }

    public OutMessageType getOutMessageType() {
        return outMessageType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(outMessageType);
    }

}
