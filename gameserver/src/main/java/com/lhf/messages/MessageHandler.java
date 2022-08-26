package com.lhf.messages;

import java.util.HashMap;
import java.util.Map;

public interface MessageHandler {

    public void setSuccessor(MessageHandler successor);

    public MessageHandler getSuccessor();

    public default void intercept(MessageHandler interceptor) {
        interceptor.setSuccessor(this.getSuccessor());
        this.setSuccessor(interceptor);
    }

    public abstract Map<CommandMessage, String> getCommands();

    public abstract CommandContext addSelfToContext(CommandContext ctx);

    public default Map<CommandMessage, String> gatherHelp(CommandContext ctx) {
        if (ctx == null) {
            ctx = new CommandContext();
        }
        ctx = this.addSelfToContext(ctx);
        Map<CommandMessage, String> myCommands = this.getCommands();
        if (myCommands == null) {
            myCommands = new HashMap<>();
        }
        if (this.getSuccessor() == null) {
            return myCommands;
        }
        Map<CommandMessage, String> received = this.getSuccessor().gatherHelp(ctx);
        if (received == null) {
            received = new HashMap<>();
        }
        received.putAll(myCommands); // override received with mine
        return received;
    }

    public default Boolean handleMessage(CommandContext ctx, Command msg) {
        if (this.getSuccessor() != null) {
            return this.getSuccessor().handleMessage(ctx, msg);
        }
        return false;
    }

}
