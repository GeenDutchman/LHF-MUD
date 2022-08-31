package com.lhf.messages;

import java.util.EnumMap;
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

    public default EnumMap<CommandMessage, String> gatherHelp(CommandContext ctx) {
        if (ctx == null) {
            ctx = new CommandContext();
        }
        ctx = this.addSelfToContext(ctx);
        EnumMap<CommandMessage, String> coalesce = new EnumMap<>(CommandMessage.class);
        Map<CommandMessage, String> myCommands = this.getCommands();
        if (myCommands != null) {
            coalesce.putAll(myCommands);
        }
        if (this.getSuccessor() == null) {
            return coalesce;
        }
        EnumMap<CommandMessage, String> received = this.getSuccessor().gatherHelp(ctx);
        if (received != null) {
            received.putAll(coalesce); // override received with mine
            return received;
        }
        return coalesce;
    }

    public default boolean handleMessage(CommandContext ctx, Command msg) {
        if (this.getSuccessor() != null) {
            return this.getSuccessor().handleMessage(ctx, msg);
        }
        return false;
    }

}
