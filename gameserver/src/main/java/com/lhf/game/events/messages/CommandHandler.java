package com.lhf.game.events.messages;

import com.lhf.game.events.GameEventContext;
import com.lhf.game.events.GameEventTypeHandler;

public interface CommandHandler extends GameEventTypeHandler {
    /**
     * Retrieves or generates the help text for a command, with respect to the
     * context
     * 
     * @param ctx
     * @return
     */
    public String getHelp(GameEventContext ctx);
}
