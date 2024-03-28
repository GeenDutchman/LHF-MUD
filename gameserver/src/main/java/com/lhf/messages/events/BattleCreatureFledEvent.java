package com.lhf.messages.events;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.lhf.game.creature.ICreature;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.messages.GameEventType;

public class BattleCreatureFledEvent extends GameEvent {
    private final ICreature runner;
    private final MultiRollResult roll;
    private final boolean fled;

    public static class Builder extends GameEvent.Builder<Builder> {
        private ICreature runner;
        private MultiRollResult roll;
        private boolean fled;

        protected Builder() {
            super(GameEventType.FLEE);
        }

        public ICreature getRunner() {
            return runner;
        }

        public Builder setRunner(ICreature runner) {
            this.runner = runner;
            return this;
        }

        public MultiRollResult getRoll() {
            return roll;
        }

        public Builder setRoll(MultiRollResult roll) {
            this.roll = roll;
            return this;
        }

        public boolean isFled() {
            return fled;
        }

        public Builder setFled(boolean fled) {
            this.fled = fled;
            return this;
        }

        @Override
        public BattleCreatureFledEvent Build() {
            return new BattleCreatureFledEvent(this);
        }

        @Override
        public Builder getThis() {
            return this;
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public BattleCreatureFledEvent(Builder builder) {
        super(builder);
        this.runner = builder.getRunner();
        this.roll = builder.getRoll();
        this.fled = builder.isFled();
    }

    @Override
    public String toString() {
        return this.printString();
    }

    @Override
    public Element buildXMLElement(Document nodeGenerator) {
        Element myElement = this.produceContentNode(nodeGenerator);
        if (myElement == null) {
            return myElement;
        }
        myElement.setAttribute("complex", "true");
        myElement.appendChild(this.addressCreatureXML(nodeGenerator, runner, true));
        if (this.fled) {
            myElement.appendChild(nodeGenerator.createTextNode(" successfully fled from the battle"));
        } else {
            myElement.appendChild(nodeGenerator.createTextNode(" attempted fleeing from the battle, but failed"));
        }
        if (!this.isBroadcast() && this.roll != null) {
            myElement.appendChild(nodeGenerator.createTextNode(" "));
            myElement.appendChild(this.roll.buildXMLElement(nodeGenerator));
        }
        myElement.appendChild(nodeGenerator.createTextNode("!"));
        return myElement;
    }

    @Override
    public String printString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.addressCreature(this.runner, true));
        if (this.fled) {
            sb.append(" successfully fled from the battle");
        } else {
            sb.append(" attempted fleeing from the battle, but failed");
        }
        if (!this.isBroadcast() && this.roll != null) {
            sb.append(" ").append(this.roll.toString());
        }
        sb.append("!");

        return sb.toString();
    }

    public ICreature getRunner() {
        return runner;
    }

    public MultiRollResult getRoll() {
        return roll;
    }

    public boolean isFled() {
        return fled;
    }

}
