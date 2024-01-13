package com.lhf.messages.events;

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
    public String toString() {
        if (this.type == null) {
            return super.toString();
        }
        StringBuilder sb = new StringBuilder();
        switch (this.type) {
            case CREATURES_ONLY:
                sb.append("You must be more than just a User to perform the action: ").append(this.commandType)
                        .append("\r\n")
                        .append("Here are the available commands:\r\n");
                break;
            case UNHANDLED:
                sb.append("That command \"").append(this.cmd).append("\" was not handled.\n")
                        .append("Here are the available commands:\r\n");
                break;
            case UNRECOGNIZED:
                sb.append("That command \"").append(this.cmd).append("\" was not recognized.\n")
                        .append("Here are the available commands:\r\n");
                break;
            case OTHER:
                sb.append("Your command\"").append(this.cmd)
                        .append("\" was really not recognized, you just have no luck, huh?\r\n");
                break;
            default:
                sb.append("Your command\"").append(this.cmd)
                        .append("\" was really not recognized, you just have no luck, huh?\r\n");

                break;
        }
        return sb.append(super.toString()).toString();
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
