package com.lhf.messages.events;

import java.util.StringJoiner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.lhf.messages.GameEventType;

public class BattleRoundWastedEvent extends BattleRoundEvent {
    private final int wastedPenalty;

    public static class Builder extends BattleRoundEvent.Builder {
        private int wastedPenalty;

        protected Builder() {
            super(GameEventType.BATTLE_TURN_WASTED);
            super.setNeedSubmission(RoundAcceptance.MISSING);
        }

        @Override
        public Builder setNeedSubmission(RoundAcceptance isSubmissionNeeded) {
            if (isSubmissionNeeded != RoundAcceptance.MISSING) {
                throw new IllegalArgumentException(
                        "Cannot set RoundAcceptance to anything other than Missing for this type");
            }
            super.setNeedSubmission(RoundAcceptance.MISSING);
            return this;
        }

        // Will coerce it to a value <= 0
        public Builder setWastedPenalty(int penalty) {
            this.wastedPenalty = penalty <= 0 ? penalty : -1 * penalty;
            return this;
        }

        public int getWastedPenalty() {
            return wastedPenalty;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public BattleRoundWastedEvent Build() {
            return new BattleRoundWastedEvent(this);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public BattleRoundWastedEvent(Builder builder) {
        super(builder);
        this.wastedPenalty = builder.getWastedPenalty();
    }

    public int getWastedPenalty() {
        return wastedPenalty;
    }

    @Override
    public String printString() {
        StringJoiner sj = new StringJoiner(" ").setEmptyValue("This is a turn notification");
        sj.add(super.toString());

        if (this.wastedPenalty != 0) {
            sj.add(this.addressCreature(this.about, true)).add("incurred a penalty of")
                    .add(Integer.toString(this.wastedPenalty)).add("damage");
        }
        return sj.toString() + "!";
    }

    @Override
    public Element buildXMLElement(Document nodeGenerator) {
        Element myElement = super.buildXMLElement(nodeGenerator);
        if (myElement == null) {
            return myElement;
        }
        if (this.wastedPenalty != 0) {
            Element wasted = nodeGenerator.createElement("WastedPenaltyDetails");
            wasted.appendChild(this.addressCreatureXML(nodeGenerator, about, true));
            wasted.appendChild(nodeGenerator.createTextNode("incurred a penalty of"));
            Element wastedNumber = nodeGenerator.createElement("WastedPenalty");
            wastedNumber.setTextContent(Integer.toString(this.wastedPenalty));
            wasted.appendChild(wastedNumber);
            wasted.appendChild(nodeGenerator.createTextNode("damage!"));
            myElement.appendChild(wasted);
        }
        return myElement;
    }

    @Override
    public String toString() {
        return this.printString();
    }

}
