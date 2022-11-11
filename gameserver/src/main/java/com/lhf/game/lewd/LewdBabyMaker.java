package com.lhf.game.lewd;

import com.lhf.game.creature.NameGenerator;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.item.concrete.LewdBed.LewdProduct;
import com.lhf.game.map.Room;

public class LewdBabyMaker implements LewdProduct {

    @Override
    public void onLewd(Room room, VrijPartij party) {
        if (room == null || party == null) {
            return;
        }
        for (String name : party.getNames()) {
            if (name.length() <= 0) {
                name = NameGenerator.GenerateSuffix(NameGenerator.GenerateGiven());
            }
            Corpse body = new Corpse(name, true);
            room.addItem(body);
        }
    }
}
