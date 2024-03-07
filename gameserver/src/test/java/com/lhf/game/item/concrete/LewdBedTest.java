package com.lhf.game.item.concrete;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.truth.Truth;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.creature.intelligence.handlers.LewdAIHandler;
import com.lhf.game.lewd.LewdBabyMaker;
import com.lhf.game.map.Room;
import com.lhf.game.map.Room.RoomBuilder;
import com.lhf.messages.MessageMatcher;
import com.lhf.messages.GameEventType;

@ExtendWith(MockitoExtension.class)
public class LewdBedTest {

    protected RoomBuilder builder = RoomBuilder.getInstance();

    @Test
    void testSolo() {
        AIComBundle first = new AIComBundle();
        Room room = this.builder.setName("Solo").quickBuild(null, null, null);
        room.addCreature(first.getNPC());
        LewdBed bed = new LewdBed(LewdBed.Builder.getInstance().setCapacity(1).setSleepSeconds(30), room);
        room.addItem(bed);

        bed.addCreature(first.getNPC());

        Truth.assertThat(bed.handleEmptyJoin(first.getNPC())).isTrue();

        MessageMatcher matcher = new MessageMatcher(GameEventType.LEWD, "meant to be shared");
        Mockito.verify(first.sssb, Mockito.timeout(1000).times(1)).send(Mockito.argThat(matcher));

        Truth.assertThat(bed.handlePopulatedJoin(first.getNPC(), null, null)).isTrue();
        Mockito.verify(first.sssb, Mockito.timeout(1000).times(2)).send(Mockito.argThat(matcher));

    }

    @Test
    void testOnePartner() {
        AIComBundle first = new AIComBundle();
        AIComBundle second = new AIComBundle();

        first.brain.addHandler(new LewdAIHandler(Set.of(second.getNPC().getName())));
        second.brain.addHandler(new LewdAIHandler(Set.of(first.getNPC().getName())));

        Room room = this.builder.setName("Pair").quickBuild(null, null, null);
        room.addCreatures(Set.of(first.getNPC(), second.getNPC()), true);
        LewdBed bed = new LewdBed(LewdBed.Builder.getInstance().setCapacity(1).setSleepSeconds(30), room);
        room.addItem(bed);

        bed.addCreature(first.getNPC());
        bed.addCreature(second.getNPC());

        Truth.assertThat(bed.handlePopulatedJoin(first.getNPC(), Set.of(second.getNPC().getName()), null)).isTrue();

        MessageMatcher matcher = new MessageMatcher(GameEventType.LEWD, "is excited to join");
        Mockito.verify(first.sssb, Mockito.timeout(1000).times(2)).send(Mockito.argThat(matcher));
        Mockito.verify(second.sssb, Mockito.timeout(1000).times(2)).send(Mockito.argThat(matcher));
        matcher = new MessageMatcher(GameEventType.LEWD, "as they do it");
        Mockito.verify(first.sssb, Mockito.timeout(1000).times(1)).send(Mockito.argThat(matcher));
        Mockito.verify(second.sssb, Mockito.timeout(1000).times(1)).send(Mockito.argThat(matcher));

    }

    @Test
    void testOnePartnerDenied() {
        AIComBundle first = new AIComBundle();
        AIComBundle second = new AIComBundle();

        Room room = this.builder.setName("Spurned").quickBuild(null, null, null);
        room.addCreatures(Set.of(first.getNPC(), second.getNPC()), true);
        LewdBed bed = new LewdBed(LewdBed.Builder.getInstance().setCapacity(1).setSleepSeconds(30), room);
        room.addItem(bed);

        bed.addCreature(first.getNPC());
        bed.addCreature(second.getNPC());

        Truth.assertThat(bed.handlePopulatedJoin(first.getNPC(), Set.of(second.getNPC().getName()), null)).isTrue();

        MessageMatcher matcher = new MessageMatcher(GameEventType.LEWD, "does not wish");

        Mockito.verify(first.sssb, Mockito.timeout(1000)).send(Mockito.argThat(matcher));
        Mockito.verify(second.sssb, Mockito.timeout(1000)).send(Mockito.argThat(matcher));
        Mockito.verify(first.sssb, Mockito.timeout(1000).atLeastOnce())
                .send(Mockito.argThat(new MessageMatcher(GameEventType.LEWD, "meant to be shared")));

    }

    @Test
    void testBabymaker() {
        AIComBundle first = new AIComBundle();
        AIComBundle second = new AIComBundle();

        first.brain.addHandler(new LewdAIHandler(Set.of(second.getNPC().getName())));
        second.brain.addHandler(new LewdAIHandler(Set.of(first.getNPC().getName())));

        Room room = this.builder.setName("Spurned").quickBuild(null, null, null);
        room.addCreatures(Set.of(first.getNPC(), second.getNPC()), false);
        LewdBed bed = new LewdBed(
                LewdBed.Builder.getInstance().setCapacity(1).setSleepSeconds(30)
                        .setLewdProduct(new LewdBabyMaker(null)),
                room);
        room.addItem(bed);

        Truth.assertThat(bed.addCreature(first.getNPC())).isTrue();
        Truth.assertThat(bed.addCreature(second.getNPC())).isTrue();

        String babyname = "veryuniquename";

        Truth.assertThat(room.getItem(babyname).isPresent()).isFalse();

        Truth.assertThat(bed.handlePopulatedJoin(first.getNPC(), Set.of(second.getNPC().getName()), Set.of(babyname)))
                .isTrue();
        MessageMatcher matcher = new MessageMatcher(GameEventType.LEWD, "as they do it");
        Mockito.verify(first.sssb, Mockito.timeout(2000).times(1)).send(Mockito.argThat(matcher));
        Mockito.verify(second.sssb, Mockito.timeout(2000).times(1)).send(Mockito.argThat(matcher));

        Truth.assertThat(room.getItem(babyname).isPresent()).isTrue();
        Truth.assertThat(room.getItem(babyname).get()).isInstanceOf(Corpse.class);
    }
}
