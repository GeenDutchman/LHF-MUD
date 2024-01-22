package com.lhf.game.creature;

import com.lhf.server.interfaces.NotNull;

public abstract class WrappedMonster extends WrappedINonPlayerCharacter<Monster> implements IMonster {

    protected WrappedMonster(@NotNull Monster monster) {
        super(monster);
    }

}