package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.google.common.truth.Truth;
import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.battle.BattleStats;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.intelligence.AIChooser;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.creature.intelligence.GroupAIRunner;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.messages.out.CreatureAffectedMessage;
import com.lhf.messages.out.StatsOutMessage;

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
        finder.npc.setFaction(CreatureFaction.RENEGADE);
        AIComBundle attacker = new AIComBundle();
        AIComBundle subAttacker = new AIComBundle();

        BattleStats battleStats = new BattleStats()
                .initialize(List.of(finder.npc, attacker.npc, subAttacker.npc));

        BattleStatsChooser chooser = new BattleStatsChooser();

        SortedMap<String, Double> targets = chooser.choose(
                Optional.of(StatsOutMessage.getBuilder().addRecords(battleStats.getBattleStatSet())
                        .Build()),
                finder.npc.getHarmMemories(),
                finder.npc.getFaction().competeSet(), List.of());

        Truth.assertThat(targets).hasSize(3); // includes finder
        for (Double value : targets.values()) {
            Truth.assertThat(value).isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                    .of(AIChooser.MIN_VALUE);
        }

        // attacker does harm but no aggro

        CreatureEffectSource source = new CreatureEffectSource("test", new EffectPersistence(TickType.INSTANT),
                null,
                "For a test", false)
                .addDamage(new DamageDice(1, DieType.HUNDRED, DamageFlavor.BLUDGEONING));

        CreatureAffectedMessage cam = CreatureAffectedMessage.getBuilder().setAffected(finder.npc)
                .setEffect(new CreatureEffect(source, attacker.npc, attacker.npc)).Build();

        finder.npc.getHarmMemories().update(cam);
        battleStats.update(cam);

        targets = chooser.choose(
                Optional.of(StatsOutMessage.getBuilder().addRecords(battleStats.getBattleStatSet())
                        .Build()),
                finder.npc.getHarmMemories(),
                finder.npc.getFaction().competeSet(), List.of());

        Truth.assertThat(targets).hasSize(3); // includes finder
        Truth.assertThat(targets.get(attacker.npc.getName()))
                .isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                .of(AIChooser.MIN_VALUE);
        Truth.assertThat(targets.get(subAttacker.npc.getName()))
                .isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                .of(AIChooser.MIN_VALUE);

        // attacker does harm with aggro

        CreatureEffectSource source2 = new CreatureEffectSource("test2",
                new EffectPersistence(TickType.INSTANT),
                null,
                "For a test", false)
                .addDamage(new DamageDice(1, DieType.SIX, DamageFlavor.AGGRO));

        CreatureAffectedMessage cam2 = CreatureAffectedMessage.getBuilder().setAffected(finder.npc)
                .setEffect(new CreatureEffect(source2, subAttacker.npc, subAttacker.npc)).Build();

        finder.npc.getHarmMemories().update(cam2);
        battleStats.update(cam2);

        targets = chooser.choose(
                Optional.of(StatsOutMessage.getBuilder().addRecords(battleStats.getBattleStatSet())
                        .Build()),
                finder.npc.getHarmMemories(),
                finder.npc.getFaction().competeSet(), List.of());

        Truth.assertThat(targets).hasSize(3); // includes finder
        Truth.assertThat(targets.get(subAttacker.npc.getName()))
                .isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                .of((double) 1);
        Truth.assertThat(targets.get(attacker.npc.getName()))
                .isWithin(AIChooser.MIN_VALUE * AIChooser.MIN_VALUE)
                .of(AIChooser.MIN_VALUE);

    }
}
