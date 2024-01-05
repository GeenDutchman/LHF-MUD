package com.lhf.game.lewd;

import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.truth.Truth;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.LewdEvent;
import com.lhf.messages.events.LewdEvent.LewdOutMessageType;

@ExtendWith(MockitoExtension.class)
public class VrijPartijTest {
    @Test
    void testAccept() {
        AIComBundle first = new AIComBundle();
        TreeSet<ICreature> party = new TreeSet<>();
        AIComBundle second = new AIComBundle();
        party.add(second.getNPC());
        VrijPartij vrijPartij = new VrijPartij(first.getNPC(), party);

        Set<ICreature> participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(first.getNPC());
        Truth.assertThat(participants).doesNotContain(second.getNPC());

        participants = vrijPartij.getParticipants(LewdAnswer.INCLUDED);
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(second.getNPC());

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(0);

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        vrijPartij.propose();

        ArgumentMatcher<GameEvent> proposeChecker = (message) -> {
            if (message == null || !GameEventType.LEWD.equals(message.getEventType())) {
                return false;
            }
            try {
                LewdEvent lom = (LewdEvent) message;
                return LewdOutMessageType.PROPOSED.equals(lom.getSubType()) && first.getNPC().equals(lom.getCreature());
            } catch (ClassCastException e) {
                return false;
            }

        };

        Mockito.verify(first.sssb, Mockito.timeout(500)).send(Mockito.argThat(proposeChecker));
        Mockito.verify(second.sssb, Mockito.timeout(500)).send(Mockito.argThat(proposeChecker));

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(first.getNPC());
        Truth.assertThat(participants).doesNotContain(second.getNPC());

        participants = vrijPartij.getParticipants(LewdAnswer.INCLUDED);
        Truth.assertThat(participants).hasSize(0);

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(second.getNPC());

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        vrijPartij.accept(second.getNPC());

        ArgumentMatcher<GameEvent> acceptanceChecker = (message) -> {
            if (message == null || !GameEventType.LEWD.equals(message.getEventType())) {
                return false;
            }
            try {
                LewdEvent lom = (LewdEvent) message;
                return LewdOutMessageType.ACCEPTED.equals(lom.getSubType())
                        && second.getNPC().equals(lom.getCreature());
            } catch (ClassCastException e) {
                return false;
            }
        };

        Mockito.verify(first.sssb, Mockito.timeout(500)).send(Mockito.argThat(acceptanceChecker));
        Mockito.verify(second.sssb, Mockito.timeout(500)).send(Mockito.argThat(acceptanceChecker));

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(first.getNPC());
        Truth.assertThat(participants).contains(second.getNPC());

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
        TreeSet<ICreature> party = new TreeSet<>();
        AIComBundle second = new AIComBundle();
        party.add(second.getNPC());
        AIComBundle third = new AIComBundle();
        party.add(third.getNPC());
        VrijPartij vrijPartij = new VrijPartij(first.getNPC(), party);

        Set<ICreature> participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(first.getNPC());
        Truth.assertThat(participants).doesNotContain(second.getNPC());

        participants = vrijPartij.getParticipants(LewdAnswer.INCLUDED);
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(second.getNPC());
        Truth.assertThat(participants).contains(third.getNPC());

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(0);

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        ArgumentMatcher<GameEvent> secondAcceptanceChecker = (message) -> {
            if (message == null || !GameEventType.LEWD.equals(message.getEventType())) {
                return false;
            }
            try {
                LewdEvent lom = (LewdEvent) message;
                return LewdOutMessageType.ACCEPTED.equals(lom.getSubType())
                        && second.getNPC().equals(lom.getCreature());
            } catch (ClassCastException e) {
                return false;
            }
        };

        Truth.assertThat(vrijPartij.accept(second.getNPC()).check()).isFalse();
        Mockito.verify(first.sssb, Mockito.timeout(500)).send(Mockito.argThat(secondAcceptanceChecker));
        Mockito.verify(second.sssb, Mockito.timeout(500)).send(Mockito.argThat(secondAcceptanceChecker));
        Mockito.verify(third.sssb, Mockito.timeout(500)).send(Mockito.argThat(secondAcceptanceChecker));

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(first.getNPC());
        Truth.assertThat(participants).contains(second.getNPC());

        participants = vrijPartij.getParticipants(LewdAnswer.INCLUDED);
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(third.getNPC());

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(0);

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        ArgumentMatcher<GameEvent> thirdAcceptanceChecker = (message) -> {
            if (message == null || !GameEventType.LEWD.equals(message.getEventType())) {
                return false;
            }
            try {
                LewdEvent lom = (LewdEvent) message;
                return LewdOutMessageType.ACCEPTED.equals(lom.getSubType()) && third.getNPC().equals(lom.getCreature());
            } catch (ClassCastException e) {
                return false;
            }
        };

        ArgumentMatcher<GameEvent> dunnitChecker = (message) -> {
            if (message == null || !GameEventType.LEWD.equals(message.getEventType())) {
                return false;
            }
            try {
                LewdEvent lom = (LewdEvent) message;
                return LewdOutMessageType.DUNNIT.equals(lom.getSubType()) && lom.getCreature() == null;
            } catch (ClassCastException e) {
                return false;
            }
        };

        Truth.assertThat(vrijPartij.accept(third.getNPC()).check()).isTrue();
        Mockito.verify(first.sssb, Mockito.timeout(500)).send(Mockito.argThat(thirdAcceptanceChecker));
        Mockito.verify(second.sssb, Mockito.timeout(500)).send(Mockito.argThat(thirdAcceptanceChecker));
        Mockito.verify(third.sssb, Mockito.timeout(500)).send(Mockito.argThat(thirdAcceptanceChecker));

        Mockito.verify(first.sssb, Mockito.timeout(500)).send(Mockito.argThat(dunnitChecker));
        Mockito.verify(second.sssb, Mockito.timeout(500)).send(Mockito.argThat(dunnitChecker));
        Mockito.verify(third.sssb, Mockito.timeout(500)).send(Mockito.argThat(dunnitChecker));

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(3);
        Truth.assertThat(participants).contains(first.getNPC());
        Truth.assertThat(participants).contains(second.getNPC());
        Truth.assertThat(participants).contains(third.getNPC());

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
        TreeSet<ICreature> party = new TreeSet<>();
        AIComBundle second = new AIComBundle();
        party.add(second.getNPC());
        AIComBundle third = new AIComBundle();
        party.add(third.getNPC());
        VrijPartij vrijPartij = new VrijPartij(first.getNPC(), party);

        Set<ICreature> participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(first.getNPC());
        Truth.assertThat(participants).doesNotContain(second.getNPC());
        Truth.assertThat(participants).doesNotContain(third.getNPC());

        vrijPartij.propose();

        ArgumentMatcher<GameEvent> proposeChecker = (message) -> {
            if (message == null || !GameEventType.LEWD.equals(message.getEventType())) {
                return false;
            }
            try {
                LewdEvent lom = (LewdEvent) message;
                return LewdOutMessageType.PROPOSED.equals(lom.getSubType()) && first.getNPC().equals(lom.getCreature());
            } catch (ClassCastException e) {
                return false;
            }

        };

        Mockito.verify(first.sssb, Mockito.timeout(500)).send(Mockito.argThat(proposeChecker));
        Mockito.verify(second.sssb, Mockito.timeout(500)).send(Mockito.argThat(proposeChecker));
        Mockito.verify(third.sssb, Mockito.timeout(500)).send(Mockito.argThat(proposeChecker));

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(second.getNPC());

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        ArgumentMatcher<GameEvent> secondAcceptanceChecker = (message) -> {
            if (message == null || !GameEventType.LEWD.equals(message.getEventType())) {
                return false;
            }
            try {
                LewdEvent lom = (LewdEvent) message;
                return LewdOutMessageType.ACCEPTED.equals(lom.getSubType())
                        && second.getNPC().equals(lom.getCreature());
            } catch (ClassCastException e) {
                return false;
            }
        };

        Truth.assertThat(vrijPartij.accept(second.getNPC()).check()).isFalse();
        Mockito.verify(first.sssb, Mockito.timeout(500)).send(Mockito.argThat(secondAcceptanceChecker));
        Mockito.verify(second.sssb, Mockito.timeout(500)).send(Mockito.argThat(secondAcceptanceChecker));
        Mockito.verify(third.sssb, Mockito.timeout(500)).send(Mockito.argThat(secondAcceptanceChecker));

        participants = vrijPartij.getParticipants();
        Truth.assertThat(participants).hasSize(2);
        Truth.assertThat(participants).contains(first.getNPC());
        Truth.assertThat(participants).contains(second.getNPC());

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(third.getNPC());

        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(0);

        ArgumentMatcher<GameEvent> thirdPass = (message) -> {
            if (message == null || !GameEventType.LEWD.equals(message.getEventType())) {
                return false;
            }
            try {
                LewdEvent lom = (LewdEvent) message;
                return LewdOutMessageType.DENIED.equals(lom.getSubType()) && third.getNPC().equals(lom.getCreature());
            } catch (ClassCastException e) {
                return false;
            }
        };

        vrijPartij.pass(third.getNPC());

        Mockito.verify(first.sssb, Mockito.timeout(500)).send(Mockito.argThat(thirdPass));
        Mockito.verify(second.sssb, Mockito.timeout(500)).send(Mockito.argThat(thirdPass));
        Mockito.verify(third.sssb, Mockito.timeout(500)).send(Mockito.argThat(thirdPass));

        ArgumentMatcher<GameEvent> dunnitChecker = (message) -> {
            if (message == null || !GameEventType.LEWD.equals(message.getEventType())) {
                return false;
            }
            try {
                LewdEvent lom = (LewdEvent) message;
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
        Truth.assertThat(participants).contains(first.getNPC());
        Truth.assertThat(participants).contains(second.getNPC());
        Truth.assertThat(participants).doesNotContain(third.getNPC());

        participants = vrijPartij.getParticipants(LewdAnswer.ASKED);
        Truth.assertThat(participants).hasSize(0);
        participants = vrijPartij.getParticipants(LewdAnswer.DENIED);
        Truth.assertThat(participants).hasSize(1);
        Truth.assertThat(participants).contains(third.getNPC());

        Mockito.verifyNoMoreInteractions(first.sssb, second.sssb, third.sssb);

    }
}
