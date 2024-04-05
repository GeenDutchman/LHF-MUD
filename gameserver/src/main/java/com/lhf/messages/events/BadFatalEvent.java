package com.lhf.messages.events;

import com.lhf.OutputBuilder;
import com.lhf.Taggable;
import com.lhf.Taggable.BasicTaggable;
import com.lhf.messages.GameEventType;

public class BadFatalEvent extends GameEvent {

    private final String extraInfo;
    private final Exception exception;

    public static class Builder extends GameEvent.Builder<Builder> {
        private String extraInfo;
        private Exception exception;

        protected Builder() {
            super(GameEventType.FATAL);
        }

        public String getExtraInfo() {
            return extraInfo;
        }

        public Builder setExtraInfo(String extraInfo) {
            this.extraInfo = extraInfo;
            return this;
        }

        public Exception getException() {
            return exception;
        }

        public Builder setException(Exception exception) {
            this.exception = exception;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public BadFatalEvent Build() {
            return new BadFatalEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    // no nonsense constructor
    public BadFatalEvent() {
        super(new Builder());
        this.extraInfo = null;
        this.exception = null;
    }

    public BadFatalEvent(Builder builder) {
        super(builder);
        this.extraInfo = builder.getExtraInfo();
        this.exception = builder.getException();
    }

    @Override
    public String toString() {
        return this.printString();
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public void buildOutput(OutputBuilder builder) {
        if (builder == null) {
            return;
        }
        builder.appendString("You made a fatal mistake");
        if (this.extraInfo != null && !this.extraInfo.isBlank()) {
            builder.appendString(this.extraInfo, ":", ".");
        } else {
            builder.appendString(".");
        }

        if (this.exception != null) {
            Taggable taggableException = BasicTaggable.customTaggable("Exception", this.exception.toString());
            builder.appendTaggable(taggableException);
        }
    }

}
