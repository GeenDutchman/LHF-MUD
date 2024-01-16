package com.lhf.game.lewd;

import com.lhf.game.creature.NameGenerator;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.map.Area;

public class LewdBabyMaker implements LewdProduct {

    @Override
    public void onLewd(Area room, VrijPartij party) {
        if (room == null || party == null) {
            return;
        }
        for (String name : party.getNames()) {
            if (name.length() <= 0) {
                name = NameGenerator.Generate(null);
            }
            Corpse body = new Corpse(name);
            room.addItem(body);
        }
    }
}
