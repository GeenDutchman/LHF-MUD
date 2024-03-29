package com.lhf.game.creature.intelligence;

import java.io.Serializable;
import java.util.Objects;
import java.util.logging.Logger;

import com.lhf.messages.GameEventType;
import com.lhf.messages.events.GameEvent;
import com.lhf.server.interfaces.NotNull;

public abstract class AIHandler implements Comparable<AIHandler>, Serializable {
    protected final String className;
    protected final transient Logger logger;
    protected final GameEventType outMessageType;

    public AIHandler(@NotNull GameEventType outMessageType) {
        this.outMessageType = outMessageType;
        this.className = this.getClass().getName();
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    public GameEventType getOutMessageType() {
        return outMessageType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(outMessageType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof AIHandler))
            return false;
        AIHandler other = (AIHandler) obj;
        return outMessageType == other.outMessageType;
    }

    @Override
    public int compareTo(AIHandler arg0) {
        return this.outMessageType.compareTo(arg0.outMessageType);
    }

    public abstract void handle(BasicAI bai, GameEvent event);

}
