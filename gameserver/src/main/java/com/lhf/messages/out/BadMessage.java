package com.lhf.messages.out;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.OutMessageType;

public class BadMessage extends HelpMessage {

    public enum BadMessageType {
        UNHANDLED, UNRECOGNIZED, OTHER, CREATURES_ONLY;
    }

    public static class Builder extends HelpMessage.AbstractBuilder<Builder> {
        private BadMessageType subType;
        private String commandEntered;
        private CommandMessage commandType;

        protected Builder() {
            super(OutMessageType.BAD_MESSAGE);
        }

        protected Builder(BadMessageType subType, Command badCommand) {
            super(OutMessageType.BAD_MESSAGE);
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

        public CommandMessage getCommandType() {
            return this.commandType;
        }

        public String getCommandEntered() {
            return this.commandEntered;
        }

        @Override
        public BadMessage Build() {
            return new BadMessage(this);
        }

        @Override
        public Builder getThis() {
            return this;
        }

    }

    private final BadMessageType type;
    private final String cmd;
    private final CommandMessage commandType;

    public static Builder getBuilder() {
        return new Builder();
    }

    protected BadMessage(Builder builder) {
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

    public CommandMessage getCommandType() {
        return this.commandType;
    }

}
