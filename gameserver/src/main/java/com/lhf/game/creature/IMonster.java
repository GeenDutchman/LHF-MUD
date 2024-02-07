package com.lhf.game.creature;

import com.lhf.game.enums.CreatureFaction;

public interface IMonster extends INonPlayerCharacter {
    public static final String defaultConvoTreeName = "non_verbal_default";

    @Override
    public default void restoreFaction() {
        this.setFaction(CreatureFaction.MONSTER);
    }

    public abstract long getMonsterNumber();

}
