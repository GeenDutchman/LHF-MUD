package com.lhf.messages.out;

import java.util.StringJoiner;

import com.lhf.messages.GameEventType;

public class WelcomeMessage extends OutMessage {

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends OutMessage.Builder<T> {

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
        public WelcomeMessage Build() {
            return new WelcomeMessage(this);
        }
    }

    public static Builder getWelcomeBuilder() {
        return new Builder();
    }

    public WelcomeMessage(AbstractBuilder<?> builder) {
        super(builder);
    }

    public String toString() {
        StringJoiner sj = new StringJoiner("\r\n");
        sj.add("Welcome to <title>LHF MUD</title>!");
        sj.add("<description>This is an old-school text-based adventure where multiple users can interact as they trawl the Dungeons of Ibaif!</description>");
        sj.add("If you wish to have fun with us, either log on or create a user.");
        sj.add("To create a user, use the command:");
        sj.add("<command>create \"[username]\" with \"[password]\"</command>");
        sj.add("If you wish to leave at any time, simply type:");
        sj.add("<command>exit</command>");

        return sj.toString();
    }

    @Override
    public String print() {
        return this.toString();
    }
}
