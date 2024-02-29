package com.lhf.game.creature;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.Stats;
import com.lhf.messages.events.ItemInteractionEvent;
import com.lhf.messages.events.QuestEvent;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.events.QuestEvent.QuestEventType;

public class QuestTest {
    @Test
    void testQuestFailure() {
        AIComBundle npc = new AIComBundle();
        final Integer maxhp = npc.getNPC().getStats().getOrDefault(Stats.MAXHP, 0);
        final Integer beginInt = npc.getNPC().getAttributes().getMod(Attributes.INT);
        Truth.assertThat(maxhp).isGreaterThan(0);
        QuestEffect effect = new QuestEffect(new QuestSource("questy quest", new EffectPersistence(2, TickType.ACTION),
                null, "A test quest", new Deltas().setAttributeBonusChange(Attributes.INT, 2),
                new Deltas().setStatChange(Stats.MAXHP, 1),
                new Deltas().setStatChange(Stats.MAXHP, (-1 * maxhp) + 1)),
                null, null);
        npc.getNPC().applyEffect(effect);
        Truth.assertThat(npc.getNPC().getAttributes().getMod(Attributes.INT)).isEqualTo(beginInt + 2);
        ICreature.eventAccepter.accept(npc.getNPC(), QuestEvent.getBuilder().setQuestName(effect.getName())
                .setQuestEventType(QuestEventType.FAILED).Build());
        final Integer postMaxhp = npc.getNPC().getStats().getOrDefault(Stats.MAXHP, 0);
        Truth.assertThat(postMaxhp).isEqualTo(1);
        Truth.assertThat(npc.getNPC().getAttributes().getMod(Attributes.INT)).isEqualTo(beginInt);
    }

    @Test
    void testQuestVisibility() {
        AIComBundle npc = new AIComBundle();
        final Integer maxhp = npc.getNPC().getStats().getOrDefault(Stats.MAXHP, 0);
        QuestEffect effect = new QuestEffect(new QuestSource("questy quest", new EffectPersistence(2, TickType.ACTION),
                null, "A test quest", new Deltas().setAttributeBonusChange(Attributes.INT, 2),
                new Deltas().setStatChange(Stats.MAXHP, 1),
                new Deltas().setStatChange(Stats.MAXHP, (-1 * maxhp) + 1)),
                null, null);
        npc.getNPC().applyEffect(effect);
        SeeEvent produced = npc.getNPC().produceMessage();
        Truth.assertThat(produced.toString()).contains(effect.getName());
    }

    @Test
    void testQuestSuccess() {
        AIComBundle npc = new AIComBundle();
        final Integer maxhp = npc.getNPC().getStats().getOrDefault(Stats.MAXHP, 0);
        final Integer beginInt = npc.getNPC().getAttributes().getMod(Attributes.INT);
        Truth.assertThat(maxhp).isGreaterThan(0);
        QuestEffect effect = new QuestEffect(new QuestSource("questy quest", new EffectPersistence(2, TickType.ACTION),
                null, "A test quest", new Deltas().setAttributeBonusChange(Attributes.INT, 2),
                new Deltas().setStatChange(Stats.MAXHP, 1),
                new Deltas().setStatChange(Stats.MAXHP, (-1 * maxhp) + 1)),
                null, null);
        npc.getNPC().applyEffect(effect);
        Truth.assertThat(npc.getNPC().getAttributes().getMod(Attributes.INT)).isEqualTo(beginInt + 2);
        ICreature.eventAccepter.accept(npc.getNPC(), QuestEvent.getBuilder().setQuestName(effect.getName())
                .setQuestEventType(QuestEventType.COMPLETED).Build());
        final Integer postMaxhp = npc.getNPC().getStats().getOrDefault(Stats.MAXHP, 0);
        Truth.assertThat(postMaxhp).isEqualTo(maxhp + 1);
        Truth.assertThat(npc.getNPC().getAttributes().getMod(Attributes.INT)).isEqualTo(beginInt);
    }

    @Test
    void testIgnoreOtherEvents() {
        AIComBundle npc = new AIComBundle();
        final Integer maxhp = npc.getNPC().getStats().getOrDefault(Stats.MAXHP, 0);
        final Integer beginInt = npc.getNPC().getAttributes().getMod(Attributes.INT);
        Truth.assertThat(maxhp).isGreaterThan(0);
        QuestEffect effect = new QuestEffect(new QuestSource("questy quest", new EffectPersistence(2, TickType.ACTION),
                null, "A test quest", new Deltas().setAttributeBonusChange(Attributes.INT, 2),
                new Deltas().setStatChange(Stats.MAXHP, 1),
                new Deltas().setStatChange(Stats.MAXHP, (-1 * maxhp) + 1)),
                null, null);
        npc.getNPC().applyEffect(effect);
        Truth.assertThat(npc.getNPC().getAttributes().getMod(Attributes.INT)).isEqualTo(beginInt + 2);
        ICreature.eventAccepter.accept(npc.getNPC(),
                ItemInteractionEvent.getBuilder().setPerformed().setDescription("questy quest test").Build());
        Truth.assertThat(npc.getNPC().getAttributes().getMod(Attributes.INT)).isEqualTo(beginInt + 2);
        Truth.assertThat(npc.getNPC().getStats().getOrDefault(Stats.MAXHP, 0)).isEqualTo(maxhp);
    }
}
