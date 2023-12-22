package com.lhf.messages.out;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.game.creature.vocation.Fighter;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.messages.events.BattleStatsRequestedEvent;

public class StatsOutMessageTest {
    @Test
    void testPrint() {
        BattleStatsRequestedEvent.Builder builder = BattleStatsRequestedEvent.getBuilder();
        builder.addRecord(new BattleStatRecord("harvey", CreatureFaction.MONSTER, new Fighter(),
                HealthBuckets.CRITICALLY_INJURED));
        BattleStatsRequestedEvent message = builder.Build();
        String messageString = message.print();
        Truth.assertThat(messageString).contains(CreatureFaction.MONSTER.name());
    }
}
