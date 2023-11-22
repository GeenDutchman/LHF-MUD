package com.lhf.game.events.messages.out;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.game.creature.vocation.Fighter;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.HealthBuckets;

public class StatsOutMessageTest {
    @Test
    void testPrint() {
        StatsOutMessage.Builder builder = StatsOutMessage.getBuilder();
        builder.addRecord(new BattleStatRecord("harvey", CreatureFaction.MONSTER, new Fighter(),
                HealthBuckets.CRITICALLY_INJURED));
        StatsOutMessage message = builder.Build();
        String messageString = message.print();
        Truth.assertThat(messageString).contains(CreatureFaction.MONSTER.name());
    }
}
