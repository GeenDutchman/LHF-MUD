package com.lhf.game.lewd;

import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.NonPlayerCharacter;

public class VrijPartijTest {
    @Test
    void testAccept() {
        Creature first = new NonPlayerCharacter();
        TreeSet<Creature> party = new TreeSet<>();
        Creature second = new NonPlayerCharacter();
        party.add(second);
        VrijPartij vrijPartij = new VrijPartij(first, party);

        Set<Creature> participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(first);
        Truth.assertThat(participants).doesNotContain(second);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(second);

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        vrijPartij.accept(second);

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(first);
        Truth.assertThat(participants).contains(second);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(0);
        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

    }

    @Test
    void testAcceptAndCheck() {
        Creature first = new NonPlayerCharacter();
        TreeSet<Creature> party = new TreeSet<>();
        Creature second = new NonPlayerCharacter();
        party.add(second);
        Creature third = new NonPlayerCharacter();
        party.add(third);
        VrijPartij vrijPartij = new VrijPartij(first, party);

        Set<Creature> participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(first);
        Truth.assertThat(participants).doesNotContain(second);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(second);

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        Truth.assertThat(vrijPartij.acceptAndCheck(second)).isFalse();

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(first);
        Truth.assertThat(participants).contains(second);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(third);

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        Truth.assertThat(vrijPartij.acceptAndCheck(third)).isTrue();

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(3);
        Truth.assertThat(participants).contains(first);
        Truth.assertThat(participants).contains(second);
        Truth.assertThat(participants).contains(third);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(0);
        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);
    }

    @Test
    void testPass() {
        Creature first = new NonPlayerCharacter();
        TreeSet<Creature> party = new TreeSet<>();
        Creature second = new NonPlayerCharacter();
        party.add(second);
        Creature third = new NonPlayerCharacter();
        party.add(third);
        VrijPartij vrijPartij = new VrijPartij(first, party);

        Set<Creature> participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(first);
        Truth.assertThat(participants).doesNotContain(second);
        Truth.assertThat(participants).doesNotContain(third);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(second);

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        Truth.assertThat(vrijPartij.acceptAndCheck(second)).isFalse();

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(first);
        Truth.assertThat(participants).contains(second);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(third);

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        vrijPartij.pass(third);
        Truth.assertThat(vrijPartij.check()).isTrue();

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(first);
        Truth.assertThat(participants).contains(second);
        Truth.assertThat(participants).doesNotContain(third);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(0);
        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(third);
    }
}
