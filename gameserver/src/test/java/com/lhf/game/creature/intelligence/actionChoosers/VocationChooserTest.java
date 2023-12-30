package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.google.common.truth.Truth;
import com.lhf.game.battle.BattleStats;
import com.lhf.game.battle.BattleStats.BattleStatsQuery;
import com.lhf.game.creature.intelligence.AIChooser;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.creature.intelligence.GroupAIRunner;
import com.lhf.game.creature.vocation.DMVocation;
import com.lhf.game.creature.vocation.Fighter;
import com.lhf.game.creature.vocation.Healer;
import com.lhf.game.creature.vocation.Mage;
import com.lhf.game.enums.CreatureFaction;

public class VocationChooserTest {
        @Spy
        private GroupAIRunner aiRunner = new GroupAIRunner(false, 2, 250, TimeUnit.MILLISECONDS);

        @BeforeEach
        public void setUp() throws Exception {
                MockitoAnnotations.openMocks(this);
                AIComBundle.setAIRunner(this.aiRunner.start());
        }

        @Test
        void testChoose() {
                AIComBundle finder = new AIComBundle();
                finder.getNPC().setFaction(CreatureFaction.RENEGADE);
                AIComBundle magi = new AIComBundle();
                magi.getNPC().setVocation(new Mage());
                AIComBundle cleric = new AIComBundle();
                cleric.getNPC().setVocation(new Healer());
                AIComBundle barbarian = new AIComBundle();
                barbarian.getNPC().setVocation(new Fighter());
                AIComBundle supreme = new AIComBundle();
                supreme.getNPC().setVocation(new DMVocation());

                VocationChooser chooser = new VocationChooser();

                BattleStats battleStats = new BattleStats()
                                .initialize(List.of(finder.getNPC(), magi.getNPC(), cleric.getNPC(), barbarian.getNPC(),
                                                supreme.getNPC()));

                SortedMap<String, Double> targets = chooser.choose(
                                battleStats.getBattleStatSet(BattleStatsQuery.ONLY_LIVING).stream()
                                                .collect(Collectors.toSet()),
                                finder.getNPC().getHarmMemories(),
                                List.of());

                Truth.assertThat(targets).hasSize(5); // us being a renegade makes us included
                Truth.assertThat(targets).containsKey(magi.getNPC().getName());
                Truth.assertThat(targets.get(magi.getNPC().getName()))
                                .isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                                .of(2 / (double) 3);
                Truth.assertThat(targets).containsKey(cleric.getNPC().getName());
                Truth.assertThat(targets.get(cleric.getNPC().getName()))
                                .isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                                .of(3 / (double) 3);
                Truth.assertThat(targets).containsKey(barbarian.getNPC().getName());
                Truth.assertThat(targets.get(barbarian.getNPC().getName()))
                                .isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                                .of(1 / (double) 3);
                Truth.assertThat(targets).containsKey(supreme.getNPC().getName());
                Truth.assertThat(targets.get(supreme.getNPC().getName()))
                                .isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                                .of(AIChooser.MIN_VALUE);

        }
}
