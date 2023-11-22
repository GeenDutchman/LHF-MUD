package com.lhf.game.item.interfaces;

import com.lhf.game.events.GameEventContext;

public interface UseAction {

    boolean useAction(GameEventContext ctx, Object useOn);
}
