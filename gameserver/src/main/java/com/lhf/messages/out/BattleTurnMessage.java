package com.lhf.messages.out;

import java.util.StringJoiner;

import com.lhf.game.creature.Creature;
import com.lhf.messages.OutMessageType;

public class BattleTurnMessage extends OutMessage {
    private final Creature myTurn;
    private final boolean yesTurn;
    private final int turnCount;
    private final int roundCount;

    public static class Builder extends OutMessage.Builder<Builder> {
        private Creature currentCreature;
        private boolean yesTurn;
        private int turnCount;
        private int roundCount;

        protected Builder() {
            super(OutMessageType.BATTLE_TURN);
        }

        public Builder setCurrentCreature(Creature turn) {
            this.currentCreature = turn;
            return this.getThis();
        }

        public Creature getCurrentCreature() {
            return currentCreature;
        }

        public Builder setYesTurn(boolean isTurn) {
            this.yesTurn = isTurn;
            return this;
        }

        public boolean isYesTurn() {
            return yesTurn;
        }

        public Builder setTurnCount(int turnCount) {
            this.turnCount = turnCount;
            return this;
        }

        public int getTurnCount() {
            return turnCount;
        }

        public Builder setRoundCount(int roundCount) {
            this.roundCount = roundCount;
            return this;
        }

        public int getRoundCount() {
            return roundCount;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public BattleTurnMessage Build() {
            return new BattleTurnMessage(this);
        }

    }

    public BattleTurnMessage(Builder builder) {
        super(builder);
        this.myTurn = builder.getCurrentCreature();
        this.yesTurn = builder.isYesTurn();
        this.turnCount = builder.getTurnCount();
        this.roundCount = builder.getRoundCount();
    }

    @Override
    public String toString() {

        StringJoiner sj = new StringJoiner(" ").setEmptyValue("This is a turn notification");

        if (this.roundCount > 0) {
            sj.add("It is round").add(Integer.toString(this.roundCount));
            if (this.turnCount > 0) {
                sj.add("turn").add(Integer.toString(this.turnCount));
            }
            sj.add(":");
        }

        if (this.isBroadcast()) {
            if (this.myTurn != null) {
                sj.add(this.myTurn.getColorTaggedName());
            } else {
                sj.add("Someone here");
            }
            sj.add("now has a turn to fight! ");
        } else {
            sj.add("It is now your turn to fight! ");
        }

        return sj.toString();
    }

    @Override
    public String print() {
        return this.toString();
    }

    public Creature getMyTurn() {
        return myTurn;
    }

    public boolean isYesTurn() {
        return yesTurn;
    }

    public int getTurnCount() {
        return turnCount;
    }

    public int getRoundCount() {
        return roundCount;
    }

}
