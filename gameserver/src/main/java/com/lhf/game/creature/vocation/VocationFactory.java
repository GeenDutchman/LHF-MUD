package com.lhf.game.creature.vocation;

import com.lhf.game.creature.vocation.Vocation.VocationName;

public class VocationFactory {
    public static Vocation getVocation(VocationName vocationName, Integer level) {
        if (vocationName == null) {
            return null;
        }

        switch (vocationName) {
            case DUNGEON_MASTER:
                return new DMVocation(level);
            case FIGHTER:
                return new Fighter(level);
            case HEALER:
                return new Healer(level);
            case MAGE:
                return new Mage(level);
            default:
                return null;
        }
    }

    public static Vocation getVocation(VocationName vocationName) {
        return VocationFactory.getVocation(vocationName, null);
    }

    public static Vocation getVocation(String vocation, Integer level) {
        if (vocation == null) {
            return null;
        }
        VocationName name = VocationName.getVocationName(vocation);
        if (name == null) {
            return null;
        }
        return VocationFactory.getVocation(name, level);
    }

    public static Vocation getVocation(String vocationName) {
        return VocationFactory.getVocation(vocationName, null);
    }

}
