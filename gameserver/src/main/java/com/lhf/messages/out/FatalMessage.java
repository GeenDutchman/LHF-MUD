package com.lhf.messages.out;

import com.lhf.messages.OutMessageType;

public class FatalMessage extends OutMessage {

    private final String extraInfo;

    public static class Builder extends OutMessage.Builder<Builder> {
        private String extraInfo;

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

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public FatalMessage Build() {
            return new FatalMessage(this);
        }

    }

    // no nonsense constructor
    public FatalMessage() {
        super(new Builder());
        this.extraInfo = null;
    }

    public FatalMessage(Builder builder) {
        super(builder);
        this.extraInfo = builder.getExtraInfo();
    }

    @Override
    public String toString() {
        return "You made a fatal mistake"
                + (this.extraInfo != null && !this.extraInfo.isBlank() ? ":" + this.extraInfo : "");
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    @Override
    public String print() {
        return this.toString();
    }

}
