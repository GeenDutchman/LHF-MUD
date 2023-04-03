package com.lhf.game.battle;

import java.util.Set;

import com.lhf.game.creature.Creature;

public class FIFOInitiativeTest implements InitiativeTest<FIFOInitiative> {

    @Override
    public FIFOInitiative provideInit(Set<Creature> creatures) {
        FIFOInitiative.Builder builder = FIFOInitiative.Builder.getInstance();
        for (Creature c : creatures) {
            builder.addCreature(c);
        }
        return new FIFOInitiative(builder);
    }

}
