package com.lhf.messages.events;

import com.lhf.game.TickType;
import com.lhf.messages.GameEventType;
import com.lhf.messages.ITickEvent;

public class FightOverEvent extends GameEvent implements ITickEvent {
    public static class Builder extends GameEvent.Builder<Builder> {

        protected Builder() {
            super(GameEventType.FIGHT_OVER);
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public FightOverEvent Build() {
            return new FightOverEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public FightOverEvent(Builder builder) {
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
