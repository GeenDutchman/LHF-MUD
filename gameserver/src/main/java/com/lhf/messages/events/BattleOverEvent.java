package com.lhf.messages.events;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
    public String printString() {
        if (!this.isBroadcast()) {
            return "Take a deep breath.  You have survived this battle!";
        }
        return "The fight is over!";
    }

    @Override
    public Element buildXMLElement(Document nodeGenerator) {
        Element myElement = this.produceContentNode(nodeGenerator);
        if (myElement != null) {
            myElement.appendChild(nodeGenerator.createTextNode(
                    this.isBroadcast() ? "The fight is over!" : "Take a deep breath.  You have survived this battle!"));
        }
        return myElement;
    }

}
