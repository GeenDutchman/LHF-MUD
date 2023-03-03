package com.lhf.messages.out;

import com.lhf.messages.OutMessageType;

public class FightOverMessage extends OutMessage {
    public static class Builder extends OutMessage.Builder<Builder> {

        protected Builder(OutMessageType type) {
            super(OutMessageType.FIGHT_OVER);
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public OutMessage Build() {
            return new FightOverMessage(this);
        }

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
    public String print() {
        return this.toString();
    }

}
