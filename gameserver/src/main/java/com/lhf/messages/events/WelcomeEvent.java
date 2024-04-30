package com.lhf.messages.events;

import com.lhf.RichOutput;
import com.lhf.Taggable;
import com.lhf.messages.GameEventType;

public class WelcomeEvent extends GameEvent {

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends GameEvent.Builder<T> {

        protected AbstractBuilder(GameEventType type) {
            super(type);
        }

        protected AbstractBuilder() {
            super(GameEventType.WELCOME);
        }
    }

    public static class Builder extends AbstractBuilder<Builder> {

        protected Builder() {
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public WelcomeEvent Build() {
            return new WelcomeEvent(this);
        }
    }

    public static Builder getWelcomeBuilder() {
        return new Builder();
    }

    public WelcomeEvent(AbstractBuilder<?> builder) {
        super(builder);
    }

    public void buildOutput(RichOutput builder) {
        if (builder == null) {
            return;
        }
        builder.appendString("Welcome to").appendTaggable(Taggable.BasicTaggable.customTaggable("title", "LHF MUD"),
                " ", "!\r\n");
        builder.appendTaggable(Taggable.BasicTaggable.customTaggable("description",
                "This is an old-school text-based adventure where multiple users can interact as they trawl the Dungeons of Ibaif!"),
                " ", "\r\n");
        builder.appendString("If you wish to have fun with us, either log on or create a user.", null, "\r\n");
        builder.appendString("To create a user, use the command:", null, "\r\n");
        builder.appendTaggable(
                Taggable.BasicTaggable.customTaggable("command", "create \"[username]\" with \"[password]\""), null,
                "\r\n");
        builder.appendString("If you wish to leave at any time, simply type:", null, "\r\n");
        builder.appendTaggable(Taggable.BasicTaggable.customTaggable("command", "exit"), null, "\r\n");
    }

}
