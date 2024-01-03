package com.lhf.messages.events;

import java.util.StringJoiner;

import com.lhf.Taggable;
import com.lhf.game.TickType;
import com.lhf.game.creature.ICreature;
import com.lhf.messages.GameEventType;
import com.lhf.messages.ITickEvent;

public class CreatureDiedEvent extends GameEvent implements ITickEvent {
    private final ICreature dearlyDeparted;
    private final Taggable cause;
    private final String extraInfo;

    public static class Builder extends GameEvent.Builder<Builder> {
        private ICreature dearlyDeparted;
        private Taggable cause;
        private String extraInfo;

        protected Builder() {
            super(GameEventType.CREATURE_DIED);
        }

        public ICreature getDearlyDeparted() {
            return dearlyDeparted;
        }

        public Builder setDearlyDeparted(ICreature dearlyDeparted) {
            if (dearlyDeparted == null || dearlyDeparted.isAlive()) {
                throw new IllegalArgumentException(String.format("Cannot announce that %s is dead!", dearlyDeparted));
            }
            this.dearlyDeparted = dearlyDeparted;
            return this;
        }

        public Taggable getCause() {
            return cause;
        }

        public Builder setCause(Taggable cause) {
            this.cause = cause;
            return this;
        }

        public String getExtraInfo() {
            return extraInfo;
        }

        public Builder setExtraInfo(String extraInfo) {
            this.extraInfo = extraInfo;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public CreatureDiedEvent Build() {
            return new CreatureDiedEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public CreatureDiedEvent(Builder builder) {
        super(builder);
        this.dearlyDeparted = builder.getDearlyDeparted();
        this.cause = builder.getCause();
        this.extraInfo = builder.getExtraInfo();
    }

    public ICreature getDearlyDeparted() {
        return dearlyDeparted;
    }

    public Taggable getCause() {
        return cause;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    @Override
    public String toString() {
        ICreature dead = this.getDearlyDeparted();
        if (dead == null || dead.isAlive()) {
            return "JK!  Nobody died!";
        }
        StringJoiner sj = new StringJoiner(" ");
        sj.add(dead.getColorTaggedName()).add("has died.");
        Taggable cause = this.getCause();
        if (cause != null) {
            sj.add("They died because of:").add(cause.getColorTaggedName() + ".");
        }
        String extras = this.getExtraInfo();
        if (extras != null && !extras.isBlank()) {
            sj.add(extras);
        }
        return sj.toString();
    }

    @Override
    public TickType getTickType() {
        return TickType.DEATH;
    }

    @Override
    public String print() {
        return this.toString();
    }
}
