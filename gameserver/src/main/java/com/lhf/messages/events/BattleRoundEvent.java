package com.lhf.messages.events;

import java.util.StringJoiner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
    public String toString() {
        return this.printString();
    }

    @Override
    public String printString() {

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
    public Element buildXMLElement(Document nodeGenerator) {
        Element myElement = this.produceContentNode(nodeGenerator);
        if (myElement == null) {
            return myElement;
        }
        myElement.setAttribute("complex", "true");

        if (this.roundCount != null) {
            myElement.appendChild(nodeGenerator.createTextNode("It is round "));
            Element roundElement = nodeGenerator.createElement("RoundCount");
            roundElement.setTextContent(Integer.toString(this.roundCount));
            myElement.appendChild(roundElement);
            myElement.appendChild(nodeGenerator.createTextNode("of the fight."));
        }

        Element submissionDetails = nodeGenerator.createElement("SubmissionDetails");
        submissionDetails.setAttribute("complex", "true");
        submissionDetails.setAttribute("colored", "false");
        if (this.needSubmission == null) {
            submissionDetails.appendChild(this.addressCreatureXML(nodeGenerator, about, true));
            submissionDetails
                    .appendChild(nodeGenerator.createTextNode("should enter an action to take for the round."));
        } else {
            switch (this.needSubmission) {
            case MISSING:
                submissionDetails.appendChild(this.addressCreatureXML(nodeGenerator, about, true));
                submissionDetails
                        .appendChild(nodeGenerator.createTextNode("had not submitted any action for the round."));
                break;
            case PERFORMED:
                submissionDetails.appendChild(this.posessiveCreatureXML(nodeGenerator, about, true));
                submissionDetails.appendChild(nodeGenerator.createTextNode("action for the round has been performed."));
                break;
            case ACCEPTED:
                submissionDetails.appendChild(this.posessiveCreatureXML(nodeGenerator, about, true));
                submissionDetails.appendChild(nodeGenerator.createTextNode("action has been submitted for the round."));
                break;
            case REJECTED:
                submissionDetails.appendChild(this.addressCreatureXML(nodeGenerator, about, true));
                submissionDetails.appendChild(
                        nodeGenerator.createTextNode("had already submitted an action to take for this round."));
                break;
            case COMPLETED:
                submissionDetails.appendChild(nodeGenerator.createTextNode("This round is over!"));
                break;
            case NEEDED:
            default:
                submissionDetails.appendChild(this.addressCreatureXML(nodeGenerator, about, true));
                submissionDetails
                        .appendChild(nodeGenerator.createTextNode("should enter an action to take for the round."));
                break;
            }
        }
        myElement.appendChild(submissionDetails);
        return myElement;

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
