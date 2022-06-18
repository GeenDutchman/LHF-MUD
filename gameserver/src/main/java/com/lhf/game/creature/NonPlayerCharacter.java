package com.lhf.game.creature;

import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.enums.CreatureFaction;

public class NonPlayerCharacter extends Creature {
    public NonPlayerCharacter() {
        super();
    }

    public NonPlayerCharacter(String name, Statblock statblock) {
        super(name, statblock);
        this.setFaction(CreatureFaction.NPC);
    }
}
