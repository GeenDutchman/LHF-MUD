package com.lhf.game.creature.intelligence;

import java.util.Objects;
import java.util.logging.Logger;

import com.lhf.game.events.messages.OutMessageType;
import com.lhf.server.interfaces.NotNull;

public abstract class AIHandler implements AIChunk {
    protected Logger logger;
    protected final OutMessageType outMessageType;

    public AIHandler(@NotNull OutMessageType outMessageType) {
        this.outMessageType = outMessageType;
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    public OutMessageType getOutMessageType() {
        return outMessageType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(outMessageType);
    }

}
