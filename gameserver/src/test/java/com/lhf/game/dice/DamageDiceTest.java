package com.lhf.game.dice;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.dice.DamageDice.FlavoredRollResult;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.enums.DamageFlavor;

public class DamageDiceTest {
    @Test
    void testInstancing() {
        DamageDice dd = new DamageDice(1, DieType.SIX, DamageFlavor.AGGRO);
        RollResult rr = dd.rollDice();
        Truth.assertThat(rr).isInstanceOf(FlavoredRollResult.class);
        RollResult rr2 = rr.twice();
        Truth.assertThat(rr2).isInstanceOf(FlavoredRollResult.class);
        Truth.assertThat(rr2.getRoll()).isEqualTo(rr.getRoll() * 2);
        FlavoredRollResult frr = (FlavoredRollResult) rr;
        Truth.assertThat(frr.getDamageFlavor()).isEqualTo(DamageFlavor.AGGRO);
        FlavoredRollResult frr2 = (FlavoredRollResult) rr2;
        Truth.assertThat(frr2.getDamageFlavor()).isEqualTo(DamageFlavor.AGGRO);
        Truth.assertThat(rr2.getOrigRoll()).isEqualTo(rr.getRoll());
    }
}
