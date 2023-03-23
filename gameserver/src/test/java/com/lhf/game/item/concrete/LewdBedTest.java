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
import com.lhf.messages.OutMessageType;

@ExtendWith(MockitoExtension.class)
public class LewdBedTest {

    protected RoomBuilder builder = RoomBuilder.getInstance();

    @Test
    void testSolo() {
        AIComBundle first = new AIComBundle();
        Room room = this.builder.setName("Solo").addCreature(first.npc).build();
        LewdBed bed = new LewdBed(room, LewdBed.Builder.getInstance().setCapacity(1).setSleepSeconds(30));
        room.addItem(bed);

        bed.addCreature(first.npc);

        Truth.assertThat(bed.handleEmptyJoin(first.npc)).isTrue();

        MessageMatcher matcher = new MessageMatcher(OutMessageType.LEWD, "meant to be shared");
        Mockito.verify(first.sssb, Mockito.timeout(1000).times(1)).send(Mockito.argThat(matcher));

        Truth.assertThat(bed.handlePopulatedJoin(first.npc, null, null)).isTrue();
        Mockito.verify(first.sssb, Mockito.timeout(1000).times(2)).send(Mockito.argThat(matcher));

    }

    @Test
    void testOnePartner() {
        AIComBundle first = new AIComBundle();
        AIComBundle second = new AIComBundle();

        first.brain.addHandler(new LewdAIHandler(Set.of(second.npc)));
        second.brain.addHandler(new LewdAIHandler(Set.of(first.npc)));

        Room room = this.builder.setName("Pair").addCreature(first.npc).addCreature(second.npc).build();
        LewdBed bed = new LewdBed(room, LewdBed.Builder.getInstance().setCapacity(1).setSleepSeconds(30));
        room.addItem(bed);

        bed.addCreature(first.npc);
        bed.addCreature(second.npc);

        Truth.assertThat(bed.handlePopulatedJoin(first.npc, Set.of(second.npc.getName()), null)).isTrue();

        MessageMatcher matcher = new MessageMatcher(OutMessageType.LEWD, "is excited to join");
        Mockito.verify(first.sssb, Mockito.timeout(1000).times(2)).send(Mockito.argThat(matcher));
        Mockito.verify(second.sssb, Mockito.timeout(1000).times(2)).send(Mockito.argThat(matcher));
        matcher = new MessageMatcher(OutMessageType.LEWD, "as they do it");
        Mockito.verify(first.sssb, Mockito.timeout(1000).times(1)).send(Mockito.argThat(matcher));
        Mockito.verify(second.sssb, Mockito.timeout(1000).times(1)).send(Mockito.argThat(matcher));

    }

    @Test
    void testOnePartnerDenied() {
        AIComBundle first = new AIComBundle();
        AIComBundle second = new AIComBundle();

        Room room = this.builder.setName("Spurned").addCreature(first.npc).addCreature(second.npc).build();
        LewdBed bed = new LewdBed(room, LewdBed.Builder.getInstance().setCapacity(1).setSleepSeconds(30));
        room.addItem(bed);

        bed.addCreature(first.npc);
        bed.addCreature(second.npc);

        Truth.assertThat(bed.handlePopulatedJoin(first.npc, Set.of(second.npc.getName()), null)).isTrue();

        MessageMatcher matcher = new MessageMatcher(OutMessageType.LEWD, "does not wish");

        Mockito.verify(first.sssb, Mockito.timeout(1000)).send(Mockito.argThat(matcher));
        Mockito.verify(second.sssb, Mockito.timeout(1000)).send(Mockito.argThat(matcher));
        Mockito.verify(first.sssb, Mockito.timeout(1000).atLeastOnce())
                .send(Mockito.argThat(new MessageMatcher(OutMessageType.LEWD, "meant to be shared")));

    }

    @Test
    void testBabymaker() {
        AIComBundle first = new AIComBundle();
        AIComBundle second = new AIComBundle();

        first.brain.addHandler(new LewdAIHandler(Set.of(second.npc)));
        second.brain.addHandler(new LewdAIHandler(Set.of(first.npc)));

        Room room = this.builder.setName("Spurned").addCreature(first.npc).addCreature(second.npc).build();
        LewdBed bed = new LewdBed(room,
                LewdBed.Builder.getInstance().setCapacity(1).setSleepSeconds(30).setLewdProduct(new LewdBabyMaker()));
        room.addItem(bed);

        Truth.assertThat(bed.addCreature(first.npc)).isTrue();
        Truth.assertThat(bed.addCreature(second.npc)).isTrue();

        String babyname = "veryuniquename";

        Truth.assertThat(room.getItem(babyname).isPresent()).isFalse();

        Truth.assertThat(bed.handlePopulatedJoin(first.npc, Set.of(second.npc.getName()), Set.of(babyname)))
                .isTrue();
        MessageMatcher matcher = new MessageMatcher(OutMessageType.LEWD, "as they do it");
        Mockito.verify(first.sssb, Mockito.timeout(2000).times(1)).send(Mockito.argThat(matcher));
        Mockito.verify(second.sssb, Mockito.timeout(2000).times(1)).send(Mockito.argThat(matcher));

        Truth.assertThat(room.getItem(babyname).isPresent()).isTrue();
        Truth.assertThat(room.getItem(babyname).get()).isInstanceOf(Corpse.class);
    }
}
