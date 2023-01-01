package com.lhf.game.lewd;

import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.intelligence.AIComBundle;

public class VrijPartijTest {
    @Test
    void testAccept() {
        AIComBundle first = new AIComBundle();
        TreeSet<Creature> party = new TreeSet<>();
        AIComBundle second = new AIComBundle();
        party.add(second.npc);
        VrijPartij vrijPartij = new VrijPartij(first.npc, party);

        Set<Creature> participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(first.npc);
        Truth.assertThat(participants).doesNotContain(second.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(second.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        vrijPartij.accept(second.npc);

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(first.npc);
        Truth.assertThat(participants).contains(second.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(0);
        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);
    }

    @Test
    void testAcceptAndCheck() {
        AIComBundle first = new AIComBundle();
        TreeSet<Creature> party = new TreeSet<>();
        AIComBundle second = new AIComBundle();
        party.add(second.npc);
        AIComBundle third = new AIComBundle();
        party.add(third.npc);
        VrijPartij vrijPartij = new VrijPartij(first.npc, party);

        Set<Creature> participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(first.npc);
        Truth.assertThat(participants).doesNotContain(second.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(second.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        Truth.assertThat(vrijPartij.acceptAndCheck(second.npc)).isFalse();

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(first.npc);
        Truth.assertThat(participants).contains(second.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(third.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        Truth.assertThat(vrijPartij.acceptAndCheck(third.npc)).isTrue();

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(3);
        Truth.assertThat(participants).contains(first.npc);
        Truth.assertThat(participants).contains(second.npc);
        Truth.assertThat(participants).contains(third.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(0);
        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);
    }

    @Test
    void testPass() {
        AIComBundle first = new AIComBundle();
        TreeSet<Creature> party = new TreeSet<>();
        AIComBundle second = new AIComBundle();
        party.add(second.npc);
        AIComBundle third = new AIComBundle();
        party.add(third.npc);
        VrijPartij vrijPartij = new VrijPartij(first.npc, party);

        Set<Creature> participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(first.npc);
        Truth.assertThat(participants).doesNotContain(second.npc);
        Truth.assertThat(participants).doesNotContain(third.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(second.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        Truth.assertThat(vrijPartij.acceptAndCheck(second.npc)).isFalse();

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(first.npc);
        Truth.assertThat(participants).contains(second.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(third.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        vrijPartij.pass(third.npc);
        Truth.assertThat(vrijPartij.check()).isTrue();

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(first.npc);
        Truth.assertThat(participants).contains(second.npc);
        Truth.assertThat(participants).doesNotContain(third.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(0);
        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(third.npc);
    }
}
