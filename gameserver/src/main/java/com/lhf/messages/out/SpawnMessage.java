package com.lhf.messages.out;

import com.lhf.messages.GameEventType;

public class SpawnMessage extends OutMessage {

    private final String username;

    public static class Builder extends OutMessage.Builder<Builder> {
        private String username;

        protected Builder() {
            super(GameEventType.SPAWN);
        }

        public String getUsername() {
            return username;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public SpawnMessage Build() {
            return new SpawnMessage(this);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public SpawnMessage(Builder builder) {
        super(builder);
        this.username = builder.getUsername();
    }

    @Override
    public String toString() {
        return "<description>" + (username != null ? username : "Someone") + " has spawned in this room."
                + "</description>";
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String print() {
        return this.toString();
    }
}
