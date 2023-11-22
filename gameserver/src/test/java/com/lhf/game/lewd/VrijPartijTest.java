package com.lhf.game.lewd;

import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.truth.Truth;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.out.LewdOutMessage;
import com.lhf.messages.out.LewdOutMessage.LewdOutMessageType;
import com.lhf.messages.out.OutMessage;

@ExtendWith(MockitoExtension.class)
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

        participants = vrijPartij.getParticipants(LewdAnswer.INCLUDED);
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(second.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(0);

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        vrijPartij.propose();

        ArgumentMatcher<OutMessage> proposeChecker = (message) -> {
            if (message == null || !OutMessageType.LEWD.equals(message.getOutType())) {
                return false;
            }
            try {
                LewdOutMessage lom = (LewdOutMessage) message;
                return LewdOutMessageType.PROPOSED.equals(lom.getSubType()) && first.npc.equals(lom.getCreature());
            } catch (ClassCastException e) {
                return false;
            }

        };

        Mockito.verify(first.sssb, Mockito.timeout(500)).send(Mockito.argThat(proposeChecker));
        Mockito.verify(second.sssb, Mockito.timeout(500)).send(Mockito.argThat(proposeChecker));

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(first.npc);
        Truth.assertThat(participants).doesNotContain(second.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.INCLUDED);
        Truth.assertThat(participants).hasSize(0);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(second.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        vrijPartij.accept(second.npc);

        ArgumentMatcher<OutMessage> acceptanceChecker = (message) -> {
            if (message == null || !OutMessageType.LEWD.equals(message.getOutType())) {
                return false;
            }
            try {
                LewdOutMessage lom = (LewdOutMessage) message;
                return LewdOutMessageType.ACCEPTED.equals(lom.getSubType()) && second.npc.equals(lom.getCreature());
            } catch (ClassCastException e) {
                return false;
            }
        };

        Mockito.verify(first.sssb, Mockito.timeout(500)).send(Mockito.argThat(acceptanceChecker));
        Mockito.verify(second.sssb, Mockito.timeout(500)).send(Mockito.argThat(acceptanceChecker));

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(first.npc);
        Truth.assertThat(participants).contains(second.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.INCLUDED);
        Truth.assertThat(participants).hasSize(0);
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

        participants = vrijPartij.getParticipants(LewdAnswer.INCLUDED);
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(second.npc);
        Truth.assertThat(participants).contains(third.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(0);

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        ArgumentMatcher<OutMessage> secondAcceptanceChecker = (message) -> {
            if (message == null || !OutMessageType.LEWD.equals(message.getOutType())) {
                return false;
            }
            try {
                LewdOutMessage lom = (LewdOutMessage) message;
                return LewdOutMessageType.ACCEPTED.equals(lom.getSubType()) && second.npc.equals(lom.getCreature());
            } catch (ClassCastException e) {
                return false;
            }
        };

        Truth.assertThat(vrijPartij.acceptAndCheck(second.npc)).isFalse();
        Mockito.verify(first.sssb, Mockito.timeout(500)).send(Mockito.argThat(secondAcceptanceChecker));
        Mockito.verify(second.sssb, Mockito.timeout(500)).send(Mockito.argThat(secondAcceptanceChecker));
        Mockito.verify(third.sssb, Mockito.timeout(500)).send(Mockito.argThat(secondAcceptanceChecker));

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(first.npc);
        Truth.assertThat(participants).contains(second.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.INCLUDED);
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(third.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(0);

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        ArgumentMatcher<OutMessage> thirdAcceptanceChecker = (message) -> {
            if (message == null || !OutMessageType.LEWD.equals(message.getOutType())) {
                return false;
            }
            try {
                LewdOutMessage lom = (LewdOutMessage) message;
                return LewdOutMessageType.ACCEPTED.equals(lom.getSubType()) && third.npc.equals(lom.getCreature());
            } catch (ClassCastException e) {
                return false;
            }
        };

        ArgumentMatcher<OutMessage> dunnitChecker = (message) -> {
            if (message == null || !OutMessageType.LEWD.equals(message.getOutType())) {
                return false;
            }
            try {
                LewdOutMessage lom = (LewdOutMessage) message;
                return LewdOutMessageType.DUNNIT.equals(lom.getSubType()) && lom.getCreature() == null;
            } catch (ClassCastException e) {
                return false;
            }
        };

        Truth.assertThat(vrijPartij.acceptAndCheck(third.npc)).isTrue();
        Mockito.verify(first.sssb, Mockito.timeout(500)).send(Mockito.argThat(thirdAcceptanceChecker));
        Mockito.verify(second.sssb, Mockito.timeout(500)).send(Mockito.argThat(thirdAcceptanceChecker));
        Mockito.verify(third.sssb, Mockito.timeout(500)).send(Mockito.argThat(thirdAcceptanceChecker));

        Mockito.verify(first.sssb, Mockito.timeout(500)).send(Mockito.argThat(dunnitChecker));
        Mockito.verify(second.sssb, Mockito.timeout(500)).send(Mockito.argThat(dunnitChecker));
        Mockito.verify(third.sssb, Mockito.timeout(500)).send(Mockito.argThat(dunnitChecker));

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(3);
        Truth.assertThat(participants).contains(first.npc);
        Truth.assertThat(participants).contains(second.npc);
        Truth.assertThat(participants).contains(third.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.INCLUDED);
        Truth.assertThat(participants).hasSize(0);
        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(0);
        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        Mockito.verifyNoMoreInteractions(first.sssb, second.sssb, third.sssb);
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

        vrijPartij.propose();

        ArgumentMatcher<OutMessage> proposeChecker = (message) -> {
            if (message == null || !OutMessageType.LEWD.equals(message.getOutType())) {
                return false;
            }
            try {
                LewdOutMessage lom = (LewdOutMessage) message;
                return LewdOutMessageType.PROPOSED.equals(lom.getSubType()) && first.npc.equals(lom.getCreature());
            } catch (ClassCastException e) {
                return false;
            }

        };

        Mockito.verify(first.sssb, Mockito.timeout(500)).send(Mockito.argThat(proposeChecker));
        Mockito.verify(second.sssb, Mockito.timeout(500)).send(Mockito.argThat(proposeChecker));
        Mockito.verify(third.sssb, Mockito.timeout(500)).send(Mockito.argThat(proposeChecker));

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(second.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        ArgumentMatcher<OutMessage> secondAcceptanceChecker = (message) -> {
            if (message == null || !OutMessageType.LEWD.equals(message.getOutType())) {
                return false;
            }
            try {
                LewdOutMessage lom = (LewdOutMessage) message;
                return LewdOutMessageType.ACCEPTED.equals(lom.getSubType()) && second.npc.equals(lom.getCreature());
            } catch (ClassCastException e) {
                return false;
            }
        };

        Truth.assertThat(vrijPartij.acceptAndCheck(second.npc)).isFalse();
        Mockito.verify(first.sssb, Mockito.timeout(500)).send(Mockito.argThat(secondAcceptanceChecker));
        Mockito.verify(second.sssb, Mockito.timeout(500)).send(Mockito.argThat(secondAcceptanceChecker));
        Mockito.verify(third.sssb, Mockito.timeout(500)).send(Mockito.argThat(secondAcceptanceChecker));

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(first.npc);
        Truth.assertThat(participants).contains(second.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(third.npc);

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        ArgumentMatcher<OutMessage> thirdPass = (message) -> {
            if (message == null || !OutMessageType.LEWD.equals(message.getOutType())) {
                return false;
            }
            try {
                LewdOutMessage lom = (LewdOutMessage) message;
                return LewdOutMessageType.DENIED.equals(lom.getSubType()) && third.npc.equals(lom.getCreature());
            } catch (ClassCastException e) {
                return false;
            }
        };

        vrijPartij.pass(third.npc);

        Mockito.verify(first.sssb, Mockito.timeout(500)).send(Mockito.argThat(thirdPass));
        Mockito.verify(second.sssb, Mockito.timeout(500)).send(Mockito.argThat(thirdPass));
        Mockito.verify(third.sssb, Mockito.timeout(500)).send(Mockito.argThat(thirdPass));

        ArgumentMatcher<OutMessage> dunnitChecker = (message) -> {
            if (message == null || !OutMessageType.LEWD.equals(message.getOutType())) {
                return false;
            }
            try {
                LewdOutMessage lom = (LewdOutMessage) message;
                return LewdOutMessageType.DUNNIT.equals(lom.getSubType()) && lom.getCreature() == null;
            } catch (ClassCastException e) {
                return false;
            }
        };

        Truth.assertThat(vrijPartij.check()).isTrue();

        Mockito.verify(first.sssb, Mockito.timeout(500)).send(Mockito.argThat(dunnitChecker));
        Mockito.verify(second.sssb, Mockito.timeout(500)).send(Mockito.argThat(dunnitChecker));

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

        Mockito.verifyNoMoreInteractions(first.sssb, second.sssb, third.sssb);

    }
}
