package com.lhf.messages.out;

import java.util.Map;

import com.lhf.messages.CommandMessage;

public class HelpMessage extends OutMessage {
    private Map<CommandMessage, String> helps;
    private CommandMessage singleHelp;

    public HelpMessage(Map<CommandMessage, String> helps, CommandMessage singleHelp) {
        this.helps = helps;
        this.singleHelp = singleHelp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.singleHelp != null && this.helps.containsKey(this.singleHelp)) {
            sb.append(this.singleHelp.getColorTaggedName()).append(":").append("\r\n").append("<description>")
                    .append(this.helps.get(this.singleHelp)).append("</description>").append("\r\n");
        } else {
            for (CommandMessage cmdMsg : this.helps.keySet()) {
                sb.append(cmdMsg.getColorTaggedName()).append(":").append("\r\n").append("<description>")
                        .append(helps.get(cmdMsg)).append("</description>").append("\r\n");
            }
        }
        return sb.toString();
    }

    public Map<CommandMessage, String> getHelps() {
        return helps;
    }

    public CommandMessage getSingleHelp() {
        return singleHelp;
    }
}
