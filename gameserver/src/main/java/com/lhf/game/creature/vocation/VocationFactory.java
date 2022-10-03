package com.lhf.game.creature.vocation;

import com.lhf.game.creature.vocation.Vocation.VocationName;

public class VocationFactory {
    public static Vocation getVocation(String vocationName) {
        VocationName name = VocationName.getVocationName(vocationName);

        switch (name) {
            case DUNGEON_MASTER: // not allowed to just make
                return null;
            case FIGHTER:
                return new Fighter();
            case HEALER:
                return new Healer();
            case MAGE:
                return new Mage();
            default:
                return null;
        }
    }
}
