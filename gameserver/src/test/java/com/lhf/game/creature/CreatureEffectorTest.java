package com.lhf.game.creature;

import org.junit.Test;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.CreatureEffector.BasicCreatureEffector;
import com.lhf.game.enums.Stats;

public class CreatureEffectorTest {
    @Test
    public void testSerialization() {
        CreatureEffector effector = new BasicCreatureEffector(null, this, new EffectPersistence(TickType.CONDITIONAL))
                        .addStatChange(Stats.AC, this.AC);
    }
}
