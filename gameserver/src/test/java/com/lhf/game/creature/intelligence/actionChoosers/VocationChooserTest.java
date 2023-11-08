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
import com.lhf.game.creature.intelligence.AIChooser;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.creature.intelligence.GroupAIRunner;
import com.lhf.game.creature.vocation.DMV;
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
                finder.npc.setFaction(CreatureFaction.RENEGADE);
                AIComBundle magi = new AIComBundle();
                magi.npc.setVocation(new Mage());
                AIComBundle cleric = new AIComBundle();
                cleric.npc.setVocation(new Healer());
                AIComBundle barbarian = new AIComBundle();
                barbarian.npc.setVocation(new Fighter());
                AIComBundle supreme = new AIComBundle();
                supreme.npc.setVocation(new DMV());

                VocationChooser chooser = new VocationChooser();

                BattleStats battleStats = new BattleStats()
                                .initialize(List.of(finder.npc, magi.npc, cleric.npc, barbarian.npc, supreme.npc));

                SortedMap<String, Double> targets = chooser.choose(
                                battleStats.getBattleStatSet().stream().collect(Collectors.toSet()),
                                finder.npc.getHarmMemories(),
                                List.of());

                Truth.assertThat(targets).hasSize(5); // us being a renegade makes us included
                Truth.assertThat(targets).containsKey(magi.npc.getName());
                Truth.assertThat(targets.get(magi.npc.getName())).isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                                .of(2 / (double) 3);
                Truth.assertThat(targets).containsKey(cleric.npc.getName());
                Truth.assertThat(targets.get(cleric.npc.getName())).isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                                .of(3 / (double) 3);
                Truth.assertThat(targets).containsKey(barbarian.npc.getName());
                Truth.assertThat(targets.get(barbarian.npc.getName()))
                                .isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                                .of(1 / (double) 3);
                Truth.assertThat(targets).containsKey(supreme.npc.getName());
                Truth.assertThat(targets.get(supreme.npc.getName())).isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                                .of(AIChooser.MIN_VALUE);

        }
}
