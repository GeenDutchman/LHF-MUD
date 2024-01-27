package com.lhf.game.creature;

import com.lhf.server.interfaces.NotNull;

public abstract class WrappedNPC extends WrappedINonPlayerCharacter<NonPlayerCharacter> {

    /**
     * Note that this can mask a Monster
     */
    protected WrappedNPC(@NotNull NonPlayerCharacter npc) {
        super(npc);
    }

}