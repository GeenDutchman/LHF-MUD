package com.lhf.messages.out;

import com.lhf.Taggable;
import com.lhf.messages.GameEventType;

public class ReincarnateMessage extends GameEvent {
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
        public ReincarnateMessage Build() {
            return new ReincarnateMessage(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public ReincarnateMessage(Builder builder) {
        super(builder);
        this.taggedName = builder.getTaggedName();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("*******************************X_X*********************************************").append("\r\n");
        sb.append(this.taggedName).append(", You have died. Out of mercy you have been reborn back where you began.");
        return sb.toString();
    }

    public String getTaggedName() {
        return taggedName;
    }

    @Override
    public String print() {
        return this.toString();
    }

}
