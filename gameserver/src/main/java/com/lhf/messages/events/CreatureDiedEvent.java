package com.lhf.messages.events;

import com.lhf.OutputBuilder;
import com.lhf.Taggable;
import com.lhf.game.TickType;
import com.lhf.game.creature.ICreature;
import com.lhf.messages.GameEventType;

public class CreatureDiedEvent extends GameEvent {
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
    public TickType getTickType() {
        return TickType.DEATH;
    }

    @Override
    public void buildOutput(OutputBuilder builder) {
        if (builder == null) {
            return;
        }
        final ICreature dead = this.getDearlyDeparted();
        if (dead == null || dead.isAlive()) {
            builder.appendString("JK! Nobody died!");
            return;
        }
        builder.appendTaggable(dead);
        builder.appendString("has died.");
        final Taggable cause = this.getCause();
        if (cause != null) {
            builder.appendString("They died because of:");
            builder.appendTaggable(cause, " ", ".");
        }
        final String extras = this.getExtraInfo();
        if (extras != null && !extras.isBlank()) {
            OutputBuilder extraBuilder = builder.produceSubBuilder("Details");
            extraBuilder.appendString(extras);
        }
    }

}
