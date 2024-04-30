package com.lhf.messages.events;

import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.Taggable.BasicTaggable;
import com.lhf.game.TickType;
import com.lhf.game.creature.ICreature;
import com.lhf.messages.GameEventType;

public class BattleRoundEvent extends GameEvent {
    public enum RoundAcceptance {
        REJECTED, ACCEPTED, NEEDED, PERFORMED, MISSING, COMPLETED;
    }

    private final RoundAcceptance needSubmission;
    private final Integer roundCount;
    protected final ICreature about;

    public static class Builder extends GameEvent.Builder<Builder> {
        private RoundAcceptance needSubmission;
        private Integer roundCount;
        private ICreature aboutCreature;

        protected Builder() {
            super(GameEventType.BATTLE_ROUND);
        }

        protected Builder(GameEventType outType) {
            super(outType != null ? outType : GameEventType.BATTLE_ROUND);
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

        public ICreature getAboutCreature() {
            return aboutCreature;
        }

        public Builder setAboutCreature(ICreature aboutCreature) {
            this.aboutCreature = aboutCreature;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public BattleRoundEvent Build() {
            return new BattleRoundEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public BattleRoundEvent(Builder builder) {
        super(builder);
        this.needSubmission = builder.getNeedSubmission();
        this.roundCount = builder.getRoundCount();
        this.about = builder.getAboutCreature();
    }

    @Override
    public void buildOutput(RichOutputBuilder builder) {
        if (builder == null) {
            return;
        }
        if (this.roundCount != null) {
            builder.appendString("It is round");
            builder.appendTaggable(BasicTaggable.customTaggable("RoundCount", Integer.toString(this.roundCount)));
            builder.appendString("of the fight.");
        }

        if (this.needSubmission == null) {
            this.addressCreature(builder, about, true);
            builder.appendString("should enter an action to take for the round.");
        } else {
            switch (this.needSubmission) {
            case MISSING:
                this.addressCreature(builder, about, true);
                builder.appendString("had not submitted any action for the round.");
                break;
            case PERFORMED:
                this.possesiveCreature(builder, about);
                builder.appendString("action for the round has been performed.");
                break;
            case ACCEPTED:
                this.possesiveCreature(builder, about);
                builder.appendString("action has been submitted for the round.");
                break;
            case REJECTED:
                this.addressCreature(builder, about);
                builder.appendString("had already submitted an action to take for this round.");
                break;
            case COMPLETED:
                builder.appendString("This round is over!");
                break;
            case NEEDED:
            default:
                this.addressCreature(builder, about, true);
                builder.appendString("should enter an action to take for the round.");
                break;
            }
        }
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

    public ICreature getAbout() {
        return about;
    }

}
