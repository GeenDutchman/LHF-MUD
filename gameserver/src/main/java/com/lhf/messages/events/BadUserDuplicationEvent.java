package com.lhf.messages.events;

import com.lhf.OutputBuilder;
import com.lhf.messages.GameEventType;

public class BadUserDuplicationEvent extends WelcomeEvent {

    public static class Builder extends WelcomeEvent.AbstractBuilder<Builder> {

        protected Builder() {
            super(GameEventType.DUPLICATE_USER);
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public BadUserDuplicationEvent Build() {
            return new BadUserDuplicationEvent(this);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public BadUserDuplicationEvent(Builder builder) {
        super(builder);
    }

    @Override
    public void buildOutput(OutputBuilder builder) {
        if (builder == null) {
            return;
        }
        super.buildOutput(builder);
        builder.appendString(
                "An adventurer by that name already exists!  Please name your adventurer something unique.");
    }

}
