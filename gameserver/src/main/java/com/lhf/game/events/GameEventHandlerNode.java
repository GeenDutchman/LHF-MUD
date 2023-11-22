package com.lhf.game.events;

import java.util.Map;

import com.lhf.game.events.GameEvent.GameEventType;
import com.lhf.game.events.messages.CommandMessage;

public interface GameEventHandlerNode {

    /**
     * Meant to be implemented by inner classes of `GameEventHandlerNode`s.
     */
    public interface GameEventTypeHandler {
        /**
         * Checks to see if a handler is enabled for given context
         * 
         * @param ctx context to examine
         * @return True if enabled, false otherwise
         */
        public boolean isEnabled(GameEventContext ctx);

        /**
         * Returns the type that the Handler is meant to handle
         * 
         * @return
         */
        public GameEventType forType();

        /**
         * Handles the event relative to the context
         * 
         * @param ctx
         * @param event
         * @return
         */
        public GameEventContext.Reply handle(GameEventContext ctx, GameEvent event);
    }

    public void setSuccessor(GameEventHandlerNode successor);

    public GameEventHandlerNode getSuccessor();

    public default void intercept(GameEventHandlerNode interceptor) {
        interceptor.setSuccessor(this.getSuccessor());
        this.setSuccessor(interceptor);
    }

    public abstract Map<CommandMessage, String> getCommands(GameEventContext ctx);

    public abstract GameEventContext addSelfToContext(GameEventContext ctx);

    public default GameEventContext.Reply handleMessage(GameEventContext ctx, GameEvent msg) {
        GameEventHandlerNode retrievedSuccessor = this.getSuccessor();
        if (retrievedSuccessor != null) {
            return retrievedSuccessor.handleMessage(ctx, msg);
        }
        return ctx.failhandle();
    }

}
