package com.lhf.messages.events;

import com.lhf.OutputBuilder;
import com.lhf.game.TickType;
import com.lhf.messages.GameEventType;

public class BattleOverEvent extends GameEvent {
    public static class Builder extends GameEvent.Builder<Builder> {

        protected Builder() {
            super(GameEventType.FIGHT_OVER);
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public BattleOverEvent Build() {
            return new BattleOverEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public BattleOverEvent(Builder builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return this.printString();
    }

    @Override
    public TickType getTickType() {
        return TickType.BATTLE;
    }

    @Override
    public void buildOutput(OutputBuilder builder) {
        if (builder == null) {
            return;
        }
        builder.appendString(
                this.isBroadcast() ? "The fight is over!" : "Take a deep breath.  You have survived this battle!");
    }

}
