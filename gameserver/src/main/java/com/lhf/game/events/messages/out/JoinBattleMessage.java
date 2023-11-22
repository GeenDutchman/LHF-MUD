package com.lhf.game.events.messages.out;

import java.util.StringJoiner;

import com.lhf.game.creature.Creature;
import com.lhf.game.events.messages.OutMessageType;

public class JoinBattleMessage extends OutMessage {
    private final Creature joiner;
    private final boolean ongoing;

    public static class Builder extends OutMessage.Builder<Builder> {
        private Creature joiner;
        private boolean ongoing;

        protected Builder() {
            super(OutMessageType.JOIN_BATTLE);
        }

        public Creature getJoiner() {
            return joiner;
        }

        public Builder setJoiner(Creature joiner) {
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
        public JoinBattleMessage Build() {
            return new JoinBattleMessage(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public JoinBattleMessage(Builder builder) {
        super(builder);
        this.joiner = builder.getJoiner();
        this.ongoing = builder.isOngoing();
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(this.addressCreature(this.joiner, true));
        sj.add("joined the");
        if (this.ongoing) {
            sj.add("ongoing");
        }
        sj.add("battle!");
        return sj.toString();
    }

    public Creature getJoiner() {
        return joiner;
    }

    public boolean isOngoing() {
        return ongoing;
    }

    @Override
    public String print() {
        return this.toString();
    }

}
