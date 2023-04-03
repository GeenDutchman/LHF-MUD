package com.lhf.game.battle;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.truth.Truth;
import com.lhf.game.creature.Creature;

public interface InitiativeTest<T extends Initiative> {

    abstract T provideInit(Set<Creature> creatures);

    public class CreatureSet {
        Creature one, two, three;

        public CreatureSet() {
            this.one = Mockito.mock(Creature.class);
            Mockito.when(one.getName()).thenReturn("one");
            Mockito.when(one.toString()).thenReturn("one");
            this.two = Mockito.mock(Creature.class);
            Mockito.when(two.getName()).thenReturn("two");
            Mockito.when(two.toString()).thenReturn("two");
            this.three = Mockito.mock(Creature.class);
            Mockito.when(three.getName()).thenReturn("three");
            Mockito.when(three.toString()).thenReturn("three");
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
