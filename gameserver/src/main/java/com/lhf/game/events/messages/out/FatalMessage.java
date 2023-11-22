package com.lhf.game.events.messages.out;

import com.lhf.game.events.messages.OutMessageType;

public class FatalMessage extends OutMessage {

    private final String extraInfo;
    private final Exception exception;

    public static class Builder extends OutMessage.Builder<Builder> {
        private String extraInfo;
        private Exception exception;

        protected Builder() {
            super(OutMessageType.FATAL);
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
        public FatalMessage Build() {
            return new FatalMessage(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    // no nonsense constructor
    public FatalMessage() {
        super(new Builder());
        this.extraInfo = null;
        this.exception = null;
    }

    public FatalMessage(Builder builder) {
        super(builder);
        this.extraInfo = builder.getExtraInfo();
        this.exception = builder.getException();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("You made a fatal mistake");
        if (this.extraInfo != null && !this.extraInfo.isBlank()) {
            sb.append(":").append(this.extraInfo);
        }
        sb.append("\n");
        sb.append("MessageUUID:").append(this.getUuid()).append("\n");
        if (this.exception != null) {
            sb.append("Error:").append(this.exception.toString());
        }
        return sb.toString();
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public String print() {
        return this.toString();
    }

}
