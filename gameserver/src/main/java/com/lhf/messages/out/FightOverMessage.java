package com.lhf.messages.out;

import com.lhf.game.TickType;
import com.lhf.messages.GameEventType;
import com.lhf.messages.ITickMessage;

public class FightOverMessage extends GameEvent implements ITickMessage {
    public static class Builder extends GameEvent.Builder<Builder> {

        protected Builder() {
            super(GameEventType.FIGHT_OVER);
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public FightOverMessage Build() {
            return new FightOverMessage(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public FightOverMessage(Builder builder) {
        super(builder);
    }

    @Override
    public String toString() {
        if (!this.isBroadcast()) {
            return "Take a deep breath.  You have survived this battle!";
        }
        return "The fight is over!";
    }

    @Override
    public TickType getTickType() {
        return TickType.BATTLE;
    }

    @Override
    public String print() {
        return this.toString();
    }

}
