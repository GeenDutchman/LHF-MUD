package com.lhf.messages.out;

import java.util.StringJoiner;

import com.lhf.messages.ITickMessage;
import com.lhf.messages.OutMessageType;
import com.lhf.game.TickType;
import com.lhf.game.creature.Creature;

public class BattleRoundMessage extends OutMessage implements ITickMessage {
    public enum RoundAcceptance {
        REJECTED, ACCEPTED, NEEDED, PERFORMED, MISSING, COMPLETED;
    }

    private final RoundAcceptance needSubmission;
    private final Integer roundCount;
    protected final Creature about;

    public static class Builder extends OutMessage.Builder<Builder> {
        private RoundAcceptance needSubmission;
        private Integer roundCount;
        private Creature aboutCreature;

        protected Builder() {
            super(OutMessageType.BATTLE_ROUND);
        }

        protected Builder(OutMessageType outType) {
            super(outType != null ? outType : OutMessageType.BATTLE_ROUND);
        }

        public Builder setNeedSubmission(RoundAcceptance isSubmissionNeeded) {
            this.needSubmission = isSubmissionNeeded;
            return this;
        }

        public RoundAcceptance getNeedSubmission() {
            return needSubmission;
        }

        public Builder setNeeded() {
            this.needSubmission = RoundAcceptance.NEEDED;
            return this;
        }

        public Builder setRoundCount(Integer roundCount) {
            this.roundCount = roundCount;
            return this;
        }

        public Integer getRoundCount() {
            return roundCount;
        }

        public Creature getAboutCreature() {
            return aboutCreature;
        }

        public Builder setAboutCreature(Creature aboutCreature) {
            this.aboutCreature = aboutCreature;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public BattleRoundMessage Build() {
            return new BattleRoundMessage(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public BattleRoundMessage(Builder builder) {
        super(builder);
        this.needSubmission = builder.getNeedSubmission();
        this.roundCount = builder.getRoundCount();
        this.about = builder.getAboutCreature();
    }

    @Override
    public String toString() {

        StringJoiner sj = new StringJoiner(" ").setEmptyValue("This is a round notification");

        if (this.roundCount != null) {
            sj.add("It is round").add(Integer.toString(this.roundCount));
            sj.add("of the fight.");
        }

        if (this.needSubmission == null) {
            sj.add(this.addressCreature(about, true)).add("should enter an action to take for the round.");
        } else {
            switch (this.needSubmission) {
                case MISSING:
                    sj.add(this.addressCreature(about, true)).add("had not submitted any action for the round.");
                    break;
                case PERFORMED:
                    sj.add(this.possesiveCreature(about, true)).add("action for the round has been performed.");
                    break;
                case ACCEPTED:
                    sj.add(this.possesiveCreature(about, true)).add("action has been submitted for the round.");
                    break;
                case REJECTED:
                    sj.add(this.addressCreature(about, true))
                            .add("had already submitted an action to take for this round.");
                    break;
                case COMPLETED:
                    sj.add("This round is over!");
                    break;
                case NEEDED:
                default:
                    sj.add(this.addressCreature(about, true)).add("should enter an action to take for the round.");
                    break;
            }
        }

        return sj.toString();
    }

    @Override
    public String print() {
        return this.toString();
    }

    public Integer getRoundCount() {
        return roundCount;
    }

    @Override
    public TickType getTickType() {
        RoundAcceptance acceptance = this.getNeedSubmission();
        if (acceptance == null) {
            return null;
        }
        switch (acceptance) {
            case ACCEPTED:
                return TickType.ACTION;
            case COMPLETED:
                return TickType.ROUND;
            case MISSING:
                return TickType.TURN;
            case NEEDED:
                return null;
            case PERFORMED:
                return TickType.TURN;
            case REJECTED:
                return null;
            default:
                return null;

        }
    }

    public RoundAcceptance getNeedSubmission() {
        return needSubmission;
    }

    public Creature getAbout() {
        return about;
    }

}
