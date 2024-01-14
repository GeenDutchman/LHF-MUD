package com.lhf.game.item.concrete;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.truth.Truth;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.map.Area;
import com.lhf.game.map.Room;
import com.lhf.messages.MessageMatcher;

public class BedTest {

    protected Room.RoomBuilder builder = Room.RoomBuilder.getInstance();

    @Test
    void testGetCapacity() {
        Area room = builder.setName("Capacity Room").quickBuild(null, null, null);
        int capacity = 2;
        Bed bed = new Bed(Bed.Builder.getInstance().setCapacity(capacity).setSleepSeconds(0), room);
        Truth.assertThat(bed.getCapacity()).isEqualTo(capacity);
    }

    @Test
    void testGetOccupancy() {
        AIComBundle first = new AIComBundle();
        AIComBundle second = new AIComBundle();
        AIComBundle third = new AIComBundle();
        Room room = builder.setName("Occupancy Room").quickBuild(null, null, null);
        room.addCreatures(Set.of(first.getNPC(), second.getNPC(), third.getNPC()), true);
        Bed bed = new Bed(Bed.Builder.getInstance().setCapacity(2).setSleepSeconds(2), room);
        room.addItem(bed);

        MessageMatcher inBed = new MessageMatcher("You are now in the bed");

        bed.doAction(first.getNPC());
        Mockito.verify(first.sssb, Mockito.timeout(500).atLeastOnce()).send(Mockito.argThat(inBed));
        Truth.assertThat(bed.getOccupancy()).isEqualTo(1);

        bed.doAction(second.getNPC());
        Mockito.verify(second.sssb, Mockito.timeout(500).atLeastOnce()).send(Mockito.argThat(inBed));
        Truth.assertThat(bed.getOccupancy()).isEqualTo(2);

        bed.doAction(third.getNPC());
        Mockito.verify(third.sssb, Mockito.after(500).never()).send(Mockito.argThat(inBed));
        Truth.assertThat(bed.getOccupancy()).isEqualTo(2);

        // one gets out

        bed.removeCreature(first.getNPC());
        Truth.assertThat(bed.getOccupancy()).isEqualTo(1);

        bed.doAction(third.getNPC());
        Mockito.verify(third.sssb, Mockito.timeout(500).atLeastOnce()).send(Mockito.argThat(inBed));
        Truth.assertThat(bed.getOccupancy()).isEqualTo(2);

    }

    @Test
    void testBedTime() {
        AIComBundle first = new AIComBundle();
        Room room = builder.setName("Sleeping Room").quickBuild(null, null, null);
        room.addCreature(first.getNPC());
        Bed bed = new Bed(Bed.Builder.getInstance().setCapacity(1).setSleepSeconds(1), room);

        MessageMatcher inBed = new MessageMatcher("You are now in the bed");

        bed.doAction(first.getNPC());
        Mockito.verify(first.sssb, Mockito.timeout(500).atLeastOnce()).send(Mockito.argThat(inBed));
        Truth.assertThat(bed.getOccupancy()).isEqualTo(1);

        Mockito.verify(first.sssb, Mockito.after(bed.sleepSeconds * 1000).atMostOnce())
                .send(Mockito.argThat(new MessageMatcher("You slept")));
    }
}
