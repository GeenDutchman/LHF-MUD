package com.lhf.game.events;

import com.lhf.game.events.GameEvent.GameEventType;

public interface GameEventTypeHandler {

    /** Used to check if this handler is enabled for such a context or at all */
    public boolean isEnabled(GameEventContext ctx);

    /** Returns the type that this handler is associated with */
    public GameEventType forType();

    /**
     * Used to handle the `GameEvent`
     * 
     * @param ctx   Context to the event
     * @param event the event to be handled
     * @return `isHandled()` should be true if handled, false if not
     */
    public GameEventContext.Reply handle(GameEventContext ctx, GameEvent event);
}
