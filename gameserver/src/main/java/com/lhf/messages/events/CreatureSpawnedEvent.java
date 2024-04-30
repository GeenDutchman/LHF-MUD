package com.lhf.messages.events;

import com.lhf.RichOutput;
import com.lhf.game.creature.ICreature;
import com.lhf.messages.GameEventType;

public class CreatureSpawnedEvent extends GameEvent {

    private final String creatureName;
    private final ICreature creature;

    public static class Builder extends GameEvent.Builder<Builder> {
        private String creatureName;
        private ICreature creature;

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

        public Builder setCreature(ICreature spawned) {
            this.creature = spawned;
            if (spawned != null) {
                this.creatureName = spawned.getName();
            }
            return this;
        }

        public ICreature getCreature() {
            return creature;
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
        this.creature = builder.getCreature();
    }

    public String getCreatureName() {
        return creatureName;
    }

    @Override
    public void buildOutput(RichOutput builder) {
        if (builder == null) {
            return;
        }
        if (this.creature != null) {
            builder.appendTaggable(creature);
        } else if (this.creatureName != null) {
            builder.appendString(creatureName);
        } else {
            builder.appendString("Someone");
        }
        builder.appendString("has spawned in this room.");
    }

}
