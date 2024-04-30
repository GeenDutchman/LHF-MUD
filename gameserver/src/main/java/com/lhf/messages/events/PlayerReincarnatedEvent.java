package com.lhf.messages.events;

import com.lhf.RichOutput;
import com.lhf.Taggable.BasicTaggable;
import com.lhf.game.creature.ICreature;
import com.lhf.messages.GameEventType;

public class PlayerReincarnatedEvent extends GameEvent {
    private final BasicTaggable reincarnator;

    public static class Builder extends GameEvent.Builder<Builder> {
        private BasicTaggable reincarnator;

        protected Builder() {
            super(GameEventType.REINCARNATION);
        }

        public BasicTaggable getReincarnator() {
            return reincarnator;
        }

        public Builder setCreature(ICreature creature) {
            if (creature != null) {
                this.reincarnator = creature.basicTaggable();
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
        this.reincarnator = builder.getReincarnator();
    }

    @Override
    public void buildOutput(RichOutput builder) {
        if (builder == null) {
            return;
        }
        builder.appendString("*******************************X_X*********************************************")
                .appendString("\r\n");
        builder.appendTaggable(reincarnator)
                .appendString(", You have died. Out of mercy you have been reborn back where you began.", null, null);
    }

    public BasicTaggable getReincarnator() {
        return reincarnator;
    }

}
