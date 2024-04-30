package com.lhf.messages.events;

import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.game.creature.ICreature;
import com.lhf.messages.GameEventType;

public class BattleJoinedEvent extends GameEvent {
    private final ICreature joiner;
    private final boolean ongoing;

    public static class Builder extends GameEvent.Builder<Builder> {
        private ICreature joiner;
        private boolean ongoing;

        protected Builder() {
            super(GameEventType.JOIN_BATTLE);
        }

        public ICreature getJoiner() {
            return joiner;
        }

        public Builder setJoiner(ICreature joiner) {
            this.joiner = joiner;
            return this;
        }

        public boolean isOngoing() {
            return ongoing;
        }

        public Builder setOngoing(boolean ongoing) {
            this.ongoing = ongoing;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public BattleJoinedEvent Build() {
            return new BattleJoinedEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public BattleJoinedEvent(Builder builder) {
        super(builder);
        this.joiner = builder.getJoiner();
        this.ongoing = builder.isOngoing();
    }

    public ICreature getJoiner() {
        return joiner;
    }

    public boolean isOngoing() {
        return ongoing;
    }

    @Override
    public void buildOutput(RichOutputBuilder builder) {
        if (builder == null) {
            return;
        }
        this.addressCreature(builder, joiner, true);
        builder.appendString(String.format("joined the %sbattle!", this.ongoing ? "ongoing" : ""));
    }

}
