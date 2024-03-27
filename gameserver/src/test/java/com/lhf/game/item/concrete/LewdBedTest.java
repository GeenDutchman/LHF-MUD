package com.lhf.game.item.concrete;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.truth.Truth;
import com.lhf.game.creature.CreatureBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INPCBuildInfo;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.creature.intelligence.handlers.LewdAIHandler;
import com.lhf.game.lewd.LewdBabyMaker;
import com.lhf.game.map.Room;
import com.lhf.game.map.Room.RoomBuilder;
import com.lhf.messages.GameEventType;
import com.lhf.messages.MessageMatcher;

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

        Truth.assertThat(bed.handlePopulatedJoin(first.getNPC(), null, null, null)).isTrue();
        Mockito.verify(first.sssb, Mockito.timeout(1000).times(2)).send(Mockito.argThat(matcher));

    }

    @Test
    void testOnePartner() {
        LewdAIHandler lewdhandler = new LewdAIHandler();
        INPCBuildInfo npcBuilder = NonPlayerCharacter.getNPCBuilder().addAIHandler(lewdhandler);
        AIComBundle first = new AIComBundle(npcBuilder);
        AIComBundle second = new AIComBundle(npcBuilder);
        lewdhandler.addPartner(second.getNPC().getName());
        lewdhandler.addPartner(first.getNPC().getName());

        Room room = this.builder.setName("Pair").quickBuild(null, null, null);
        room.addCreatures(Set.of(first.getNPC(), second.getNPC()), true);
        LewdBed bed = new LewdBed(LewdBed.Builder.getInstance().setCapacity(1).setSleepSeconds(30), room);
        room.addItem(bed);

        bed.addCreature(first.getNPC());
        bed.addCreature(second.getNPC());

        Truth.assertThat(bed.handlePopulatedJoin(first.getNPC(), Set.of(second.getNPC().getName()), null, null))
                .isTrue();

        MessageMatcher matcher = new MessageMatcher(GameEventType.LEWD, Set.of("is excited to join"), Set.of(), null,
                true);
        Mockito.verify(first.sssb, Mockito.timeout(1000).times(2)).send(Mockito.argThat(matcher));
        Mockito.verify(second.sssb, Mockito.timeout(1000).times(2)).send(Mockito.argThat(matcher));
        matcher = new MessageMatcher(GameEventType.LEWD, Set.of("as they do it"), Set.of(), null, true);
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

        Truth.assertThat(bed.handlePopulatedJoin(first.getNPC(), Set.of(second.getNPC().getName()), null, null))
                .isTrue();

        MessageMatcher matcher = new MessageMatcher(GameEventType.LEWD, Set.of("does not wish"), Set.of(), null, true);

        Mockito.verify(first.sssb, Mockito.timeout(1000)).send(Mockito.argThat(matcher));
        Mockito.verify(second.sssb, Mockito.timeout(1000)).send(Mockito.argThat(matcher));
        Mockito.verify(first.sssb, Mockito.timeout(1000).atLeastOnce()).send(Mockito
                .argThat(new MessageMatcher(GameEventType.LEWD, Set.of("meant to be shared"), Set.of(), null, true)));

    }

    @Test
    void testBabymaker() {
        LewdAIHandler lewdhandler = new LewdAIHandler();
        INPCBuildInfo npcBuilder = NonPlayerCharacter.getNPCBuilder().addAIHandler(lewdhandler);
        AIComBundle first = new AIComBundle(npcBuilder);
        AIComBundle second = new AIComBundle(npcBuilder);
        lewdhandler.addPartner(second.getNPC().getName());
        lewdhandler.addPartner(first.getNPC().getName());

        Room room = this.builder.setName("Spurned").quickBuild(null, null, null);
        room.addCreatures(Set.of(first.getNPC(), second.getNPC()), false);
        LewdBed bed = new LewdBed(LewdBed.Builder.getInstance().setCapacity(1).setSleepSeconds(30)
                .setLewdProduct(new LewdBabyMaker(null)), room);
        room.addItem(bed);

        Truth.assertThat(bed.addCreature(first.getNPC())).isTrue();
        Truth.assertThat(bed.addCreature(second.getNPC())).isTrue();

        String babyname = "veryuniquename";

        Truth.assertThat(room.getItem(babyname).isPresent()).isFalse();

        Truth.assertThat(bed.handlePopulatedJoin(first.getNPC(), Set.of(second.getNPC().getName()),
                Set.of(new CreatureBuildInfo(null).setName(babyname)), null)).isTrue();
        MessageMatcher matcher = new MessageMatcher(GameEventType.LEWD, Set.of("as they do it"), Set.of(), null, true);
        Mockito.verify(first.sssb, Mockito.timeout(2000).times(1)).send(Mockito.argThat(matcher));
        Mockito.verify(second.sssb, Mockito.timeout(2000).times(1)).send(Mockito.argThat(matcher));

        Truth.assertThat(room.getItem(babyname).isPresent()).isTrue();
        Truth.assertThat(room.getItem(babyname).get()).isInstanceOf(Corpse.class);
    }
}
