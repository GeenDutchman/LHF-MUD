package com.lhf.game.item.interfaces;

import com.lhf.messages.CommandContext;

public interface UseAction {

    boolean useAction(CommandContext ctx, Object useOn);
}
