package com.lhf.game.item.interfaces;

import com.lhf.game.events.messages.CommandContext;

public interface UseAction {

    boolean useAction(CommandContext ctx, Object useOn);
}
