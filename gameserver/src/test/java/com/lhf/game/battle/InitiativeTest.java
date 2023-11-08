package com.lhf.game.battle;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.truth.Truth;
import com.lhf.game.creature.Creature;
import com.lhf.server.client.ClientID;

public interface InitiativeTest<T extends Initiative> {

    abstract T provideInit(Set<Creature> creatures);

    public class CreatureSet {
        Creature one, two, three;
        ClientID idOne, idTwo, idThree;

        public CreatureSet() {
            this.one = Mockito.mock(Creature.class);
            Mockito.when(one.getName()).thenReturn("one");
            Mockito.when(one.toString()).thenReturn("one");
            this.idOne = new ClientID();
            Mockito.when(one.getClientID()).thenReturn(this.idOne);
            this.two = Mockito.mock(Creature.class);
            Mockito.when(two.getName()).thenReturn("two");
            Mockito.when(two.toString()).thenReturn("two");
            this.idTwo = new ClientID();
            Mockito.when(two.getClientID()).thenReturn(this.idTwo);
            this.three = Mockito.mock(Creature.class);
            Mockito.when(three.getName()).thenReturn("three");
            Mockito.when(three.toString()).thenReturn("three");
            this.idThree = new ClientID();
            Mockito.when(three.getClientID()).thenReturn(this.idThree);
        }

        public Set<Creature> asSet() {
            return Set.of(one, two, three);
        }
    }

    @Test
    default void testNextTurn() {

        T init = this.provideInit(new CreatureSet().asSet());

        init.start();

        Creature current = init.getCurrent();

        for (int round = 1; round <= 3; round++) {

            for (int turn = 1; turn <= 3; turn++) {
                Truth.assertWithMessage("Round Count").that(init.getRoundCount()).isEqualTo(round);
                Truth.assertWithMessage("Turn count").that(init.getTurnCount()).isEqualTo(turn);

                Creature next = init.nextTurn();

                Truth.assertThat(next.getName()).isNotEqualTo(current.getName());

                current = next;

            }
        }

    }
}
