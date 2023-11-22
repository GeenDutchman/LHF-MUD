package com.lhf.game.events.messages;

import java.util.Map;

import com.lhf.game.events.GameEvent;
import com.lhf.game.events.GameEvent.GameEventType;
import com.lhf.game.events.GameEventContext;
import com.lhf.game.events.GameEventHandlerNode;
import com.lhf.game.events.GameEventHandlerNode.GameEventTypeHandler;

public interface CommandHandler extends GameEventTypeHandler {
    /**
     * Retrieves or generates the help text for a command, with respect to the
     * context
     * 
     * @param ctx
     * @return
     */
    public String getHelp(GameEventContext ctx);

    /**
     * Returns the type that the Handler is meant to handle
     * 
     * @return
     */
    @Override
    public CommandMessage forType();

    // /**
    // * Handles the event relative to the context
    // *
    // * @param ctx
    // * @param event
    // * @return
    // */
    // @Override
    // public CommandContext.Reply handle(GameEventContext ctx, GameEvent event);

    public static CommandContext.Reply gatherHelps(GameEventHandlerNode node) {
        CommandContext ctx = new CommandContext();
        if (node == null) {
            return ctx.failhandle();
        }
        GameEventHandlerNode next = node;
        while (next != null) {
            next.addSelfToContext(ctx);
            Map<GameEventType, GameEventTypeHandler> handlers = next.getHandlers(ctx);
            if (handlers != null) {
                for (GameEventTypeHandler handler : handlers.values()) {
                    if (handler instanceof CommandHandler ch && ch.isEnabled(ctx)) {
                        ctx.addHelp(ch.forType(), ch.getHelp(ctx));
                    }
                }
            }
            next = next.getSuccessor();
        }
        return ctx.handled();
    }
}
