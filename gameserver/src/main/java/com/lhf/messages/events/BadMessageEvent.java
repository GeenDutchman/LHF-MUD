package com.lhf.messages.events;

import com.lhf.RichOutput;
import com.lhf.messages.Command;
import com.lhf.messages.GameEventType;
import com.lhf.messages.in.AMessageType;

public class BadMessageEvent extends HelpNeededEvent {

    public enum BadMessageType {
        UNHANDLED, UNRECOGNIZED, OTHER, CREATURES_ONLY;
    }

    public static class Builder extends HelpNeededEvent.AbstractBuilder<Builder> {
        private BadMessageType subType;
        private String commandEntered;
        private AMessageType commandType;

        protected Builder() {
            super(GameEventType.BAD_MESSAGE);
        }

        protected Builder(BadMessageType subType, Command badCommand) {
            super(GameEventType.BAD_MESSAGE);
            this.subType = subType;
            this.setCommand(badCommand);
        }

        public Builder setBadMessageType(BadMessageType subType) {
            this.subType = subType;
            return this.getThis();
        }

        public BadMessageType getSubType() {
            return this.subType;
        }

        public Builder setCommand(Command badCommand) {
            this.commandType = badCommand.getType();
            this.commandEntered = badCommand.getWhole();
            return this.getThis();
        }

        public AMessageType getCommandType() {
            return this.commandType;
        }

        public String getCommandEntered() {
            return this.commandEntered;
        }

        @Override
        public BadMessageEvent Build() {
            return new BadMessageEvent(this);
        }

        @Override
        public Builder getThis() {
            return this;
        }

    }

    private final BadMessageType type;
    private final String cmd;
    private final AMessageType commandType;

    public static Builder getBuilder() {
        return new Builder();
    }

    protected BadMessageEvent(Builder builder) {
        super(builder);
        this.type = builder.getSubType();
        this.cmd = builder.getCommandEntered();
        this.commandType = builder.getCommandType();
    }

    @Override
    public void buildOutput(RichOutput builder) {
        if (builder == null) {
            return;
        }
        if (this.type == null) {
            super.buildOutput(builder);
            return;
        }
        switch (this.type) {
        case CREATURES_ONLY:
            builder.appendString("You must be more than just a User to perform the action:")
                    .appendTaggable(this.commandType, " ", "\r\n");
            builder.appendString("Here are the available commands:\r\n");
            break;
        case UNHANDLED:
            builder.appendString("That command \"", null, null).appendString(this.cmd)
                    .appendString("\" was not handled.\r\n", null, null);
            builder.appendString("Here are the available commands:\r\n");
            break;
        case UNRECOGNIZED:
            builder.appendString("That command \"", null, null).appendString(this.cmd)
                    .appendString("\" was not recognized.\r\n", null, null);
            builder.appendString("Here are the available commands:\r\n");

            break;
        case OTHER:
            // fallthrough
        default:
            builder.appendString("Your command \"", null, null).appendString(this.cmd)
                    .appendString("\" was not recognized, you just have no luck, huh?\r\n", null, null);

            break;
        }
        super.buildOutput(builder);
    }

    public BadMessageType getType() {
        return type;
    }

    public String getCommandString() {
        return this.cmd;
    }

    public AMessageType getCommandType() {
        return this.commandType;
    }

}
