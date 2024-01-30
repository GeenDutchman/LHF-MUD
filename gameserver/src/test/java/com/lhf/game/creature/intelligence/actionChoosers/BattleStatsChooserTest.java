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
import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.battle.BattleStats;
import com.lhf.game.battle.BattleStats.BattleStatRecord.BattleStat;
import com.lhf.game.battle.BattleStats.BattleStatsQuery;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.creature.intelligence.AIChooser;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.creature.intelligence.GroupAIRunner;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.dice.DamageDice.FlavoredRollResult;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.messages.events.CreatureAffectedEvent;

public class BattleStatsChooserTest {
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

                BattleStatsChooser chooser = new BattleStatsChooser();

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

                // attacker does harm but no aggro

                CreatureEffectSource source = new CreatureEffectSource("test", new EffectPersistence(TickType.INSTANT),
                                null,
                                "For a test", new Deltas()
                                                .addDamage(new DamageDice(1, DieType.HUNDRED,
                                                                DamageFlavor.BLUDGEONING)));

                CreatureAffectedEvent cam = CreatureAffectedEvent.getBuilder().setAffected(finder.getNPC())
                                .setEffect(new CreatureEffect(source, attacker.getNPC(), attacker.getNPC())).Build();

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
                                .of(AIChooser.MIN_VALUE);
                Truth.assertThat(targets.get(subAttacker.getNPC().getName()))
                                .isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                                .of(AIChooser.MIN_VALUE);

                // attacker does harm with aggro

                CreatureEffectSource source2 = new CreatureEffectSource("test2",
                                new EffectPersistence(TickType.INSTANT),
                                null,
                                "For a test", new Deltas()
                                                .addDamage(new DamageDice(1, DieType.SIX, DamageFlavor.AGGRO)));

                CreatureAffectedEvent cam2 = CreatureAffectedEvent.getBuilder().setAffected(finder.getNPC())
                                .setEffect(new CreatureEffect(source2, subAttacker.getNPC(), subAttacker.getNPC()))
                                .Build();

                finder.getNPC().getHarmMemories().update(cam2);
                battleStats.update(cam2);

                targets = chooser.choose(
                                battleStats.getBattleStatSet(BattleStatsQuery.ONLY_LIVING).stream()
                                                .collect(Collectors.toSet()),
                                finder.getNPC().getHarmMemories(),
                                List.of());

                Truth.assertThat(targets).hasSize(3); // includes finder
                Truth.assertThat(targets.get(subAttacker.getNPC().getName()))
                                .isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                                .of((double) 1);
                Truth.assertThat(targets.get(attacker.getNPC().getName()))
                                .isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                                .of(AIChooser.MIN_VALUE);

        }

        @Test
        void testAggroAccumulation() {
                AIComBundle finder = new AIComBundle();
                finder.getNPC().setFaction(CreatureFaction.RENEGADE);
                AIComBundle attacker = new AIComBundle();

                BattleStats battleStats = new BattleStats()
                                .initialize(List.of(finder.getNPC(), attacker.getNPC()));

                CreatureEffectSource source = new CreatureEffectSource("test", new EffectPersistence(TickType.INSTANT),
                                null,
                                "For a test", new Deltas()
                                                .addDamage(new DamageDice(1, DieType.HUNDRED,
                                                                DamageFlavor.BLUDGEONING))
                                                .addDamage(new DamageDice(2, DieType.SIX, DamageFlavor.AGGRO)));

                MultiRollResult.Builder mrrBuilder = new MultiRollResult.Builder();
                CreatureEffect effect = new CreatureEffect(source, attacker.getNPC(), attacker.getNPC());

                for (RollResult rr : effect.getApplicationDamageResult()) {
                        if (rr instanceof FlavoredRollResult) {
                                FlavoredRollResult frr = (FlavoredRollResult) rr;
                                if (DamageFlavor.AGGRO.equals(frr.getDamageFlavor())) {
                                        mrrBuilder.addRollResults(frr.none());
                                } else {
                                        mrrBuilder.addRollResults(frr.negative());
                                }
                        }
                }

                effect.updateApplicationDamage(mrrBuilder.Build());

                CreatureAffectedEvent cam = CreatureAffectedEvent.getBuilder().setAffected(finder.getNPC())
                                .setEffect(effect).Build();

                System.out.println(cam.print());

                battleStats.update(cam);

                System.out.println(battleStats.toString());

                Truth.assertThat(
                                battleStats.getBattleStats(BattleStatsQuery.ONLY_LIVING)
                                                .get(attacker.getNPC().getName())
                                                .getStats()
                                                .get(BattleStat.AGGRO_DAMAGE))
                                .isAtLeast(1);
        }
}
