package com.lhf.messages.out;

import java.util.Map;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.OutMessageType;

public class BadMessage extends HelpMessage {
    public enum BadMessageType {
        UNHANDLED, UNRECOGNIZED, OTHER;
    }

    private BadMessageType type;
    private Command cmd;

    public BadMessage(BadMessageType type, Map<CommandMessage, String> helps, Command cmd) {
        super(helps, cmd.getType());
        this.retype(OutMessageType.BAD_MESSAGE);
        this.type = type;
        this.cmd = cmd;
    }

    @Override
    public String toString() {
        if (this.type == null) {
            return super.toString();
        }
        StringBuilder sb = new StringBuilder();
        switch (this.type) {
            case UNHANDLED:
                sb.append("That command \"").append(cmd.getWhole()).append("\" was not handled.\n")
                        .append("Here are the available commands:\r\n");
                break;
            case UNRECOGNIZED:
                sb.append("That command \"").append(cmd.getWhole()).append("\" was not recognized.\n")
                        .append("Here are the available commands:\r\n");
                break;
            case OTHER:
                sb.append("Your command\"").append(cmd.getWhole())
                        .append("\" was really not recognized, you just have no luck, huh?\r\n");
                break;
            default:
                sb.append("Your command\"").append(cmd.getWhole())
                        .append("\" was really not recognized, you just have no luck, huh?\r\n");

                break;
        }
        return sb.append(super.toString()).toString();
    }

    public BadMessageType getType() {
        return type;
    }

    public Command getCmd() {
        return cmd;
    }

}
