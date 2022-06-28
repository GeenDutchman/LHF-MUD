package com.lhf.game.creature;

import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.enums.CreatureFaction;

//  TODO:  https://gamedev.stackexchange.com/questions/12458/how-to-manage-all-the-npc-ai-objects-on-the-server/12512#12512

public class NonPlayerCharacter extends Creature {
    public NonPlayerCharacter() {
        super();
    }

    public NonPlayerCharacter(String name, Statblock statblock) {
        super(name, statblock);
        this.setFaction(CreatureFaction.NPC);
    }
}
