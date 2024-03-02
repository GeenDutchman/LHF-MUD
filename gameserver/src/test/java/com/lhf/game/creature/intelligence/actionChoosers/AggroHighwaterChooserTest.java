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
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.creature.intelligence.AIChooser;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.creature.intelligence.GroupAIRunner;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.messages.events.CreatureAffectedEvent;

public class AggroHighwaterChooserTest {
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
                AIComBundle attacker = new AIComBundle();
                AIComBundle subAttacker = new AIComBundle();

                BattleStats battleStats = new BattleStats()
                                .initialize(List.of(finder.getNPC(), attacker.getNPC(), subAttacker.getNPC()));

                AggroHighwaterChooser chooser = new AggroHighwaterChooser();

                SortedMap<String, Double> targets = chooser.choose(
                                battleStats.getBattleStatSet(BattleStatsQuery.ONLY_LIVING).stream()
                                                .collect(Collectors.toSet()),
                                finder.getNPC().getHarmMemories(),
                                List.of());

                Truth.assertThat(targets).hasSize(3); // includes finder
                for (Double value : targets.values()) {
                        Truth.assertThat(value).isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                                        .of(AIChooser.MIN_VALUE);
                }

                // attacker does some harm

                CreatureEffectSource source = new CreatureEffectSource.Builder("test").instantPersistence()
                                .setDescription("For a test")
                                .setOnApplication(new Deltas()
                                                .addDamage(new DamageDice(6, DieType.SIX, DamageFlavor.BLUDGEONING)))
                                .build();

                CreatureAffectedEvent cam = CreatureAffectedEvent.getBuilder().setAffected(finder.getNPC())
                                .fromCreatureEffect(new CreatureEffect(source, attacker.getNPC(), attacker.getNPC()))
                                .Build();

                finder.getNPC().getHarmMemories().update(cam);
                battleStats.update(cam);

                targets = chooser.choose(
                                battleStats.getBattleStatSet(BattleStatsQuery.ONLY_LIVING).stream()
                                                .collect(Collectors.toSet()),
                                finder.getNPC().getHarmMemories(),
                                List.of());

                Truth.assertThat(targets).hasSize(3); // includes finder
                Truth.assertThat(targets.get(attacker.getNPC().getName()))
                                .isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                                .of(chooser.getWeight());
                Truth.assertThat(targets.get(subAttacker.getNPC().getName()))
                                .isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                                .of(AIChooser.MIN_VALUE);

                // subattacker does not enough harm

                CreatureEffectSource source2 = new CreatureEffectSource.Builder("test2").instantPersistence()
                                .setDescription("For a test")
                                .setOnApplication(new Deltas()
                                                .addDamage(new DamageDice(1, DieType.SIX, DamageFlavor.AGGRO)))
                                .build();

                CreatureAffectedEvent cam2 = CreatureAffectedEvent.getBuilder().setAffected(finder.getNPC())
                                .fromCreatureEffect(
                                                new CreatureEffect(source2, subAttacker.getNPC(), subAttacker.getNPC()))
                                .Build();

                finder.getNPC().getHarmMemories().update(cam2);
                battleStats.update(cam2);

                targets = chooser.choose(
                                battleStats.getBattleStatSet(BattleStatsQuery.ONLY_LIVING).stream()
                                                .collect(Collectors.toSet()),
                                finder.getNPC().getHarmMemories(),
                                List.of());

                Truth.assertThat(targets).hasSize(3); // includes finder
                Truth.assertThat(targets.get(attacker.getNPC().getName()))
                                .isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                                .of(chooser.getWeight());
                Truth.assertThat(targets.get(subAttacker.getNPC().getName()))
                                .isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                                .of(AIChooser.MIN_VALUE);

        }
}
