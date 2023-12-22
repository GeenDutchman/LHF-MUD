package com.lhf.messages.out;

import com.lhf.messages.GameEventType;

public class CreatureSpawnedEvent extends GameEvent {

    private final String creatureName;

    public static class Builder extends GameEvent.Builder<Builder> {
        private String creatureName;

        protected Builder() {
            super(GameEventType.SPAWN);
        }

        public String getCreatureName() {
            return creatureName;
        }

        public Builder setCreatureName(String creatureName) {
            this.creatureName = creatureName;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public CreatureSpawnedEvent Build() {
            return new CreatureSpawnedEvent(this);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public CreatureSpawnedEvent(Builder builder) {
        super(builder);
        this.creatureName = builder.getCreatureName();
    }

    @Override
    public String toString() {
        return "<description>" + (creatureName != null ? creatureName : "Someone") + " has spawned in this room."
                + "</description>";
    }

    public String getCreatureName() {
        return creatureName;
    }

    @Override
    public String print() {
        return this.toString();
    }
}
