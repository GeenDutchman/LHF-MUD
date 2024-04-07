package com.lhf.messages.events;

import com.lhf.OutputBuilder;
import com.lhf.Taggable;
import com.lhf.messages.GameEventType;

public class PlayerReincarnatedEvent extends GameEvent {
    private final String taggedName;

    public static class Builder extends GameEvent.Builder<Builder> {
        private String taggedName;

        protected Builder() {
            super(GameEventType.REINCARNATION);
        }

        public String getTaggedName() {
            return taggedName;
        }

        public Builder setTaggedName(String taggedName) {
            this.taggedName = taggedName;
            return this;
        }

        public Builder setTaggedName(Taggable taggable) {
            if (taggable != null) {
                this.taggedName = taggable.getColorTaggedName();
            }
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public PlayerReincarnatedEvent Build() {
            return new PlayerReincarnatedEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public PlayerReincarnatedEvent(Builder builder) {
        super(builder);
        this.taggedName = builder.getTaggedName();
    }

    @Override
    public void buildOutput(OutputBuilder builder) {
        if (builder == null) {
            return;
        }
        builder.appendString("*******************************X_X*********************************************")
                .appendString("\r\n");
        builder.appendString(taggedName)
                .appendString(", You have died. Out of mercy you have been reborn back where you began.", null, null);
    }

    public String getTaggedName() {
        return taggedName;
    }

}
