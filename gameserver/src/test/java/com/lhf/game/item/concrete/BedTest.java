package com.lhf.game.item.concrete;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.truth.Truth;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.map.Area;
import com.lhf.game.map.Room;
import com.lhf.messages.MessageMatcher;
import com.lhf.messages.events.GameEvent;

public class BedTest {

    protected Room.RoomBuilder builder = Room.RoomBuilder.getInstance();

    @Test
    void testGetCapacity() {
        Area room = builder.setName("Capacity Room").quickBuild(null, null, null);
        int capacity = 2;
        Bed bed = new Bed(room, Bed.Builder.getInstance().setCapacity(capacity).setSleepSeconds(0));
        Truth.assertThat(bed.getCapacity()).isEqualTo(capacity);
    }

    @Test
    void testGetOccupancy() {
        AIComBundle first = new AIComBundle();
        AIComBundle second = new AIComBundle();
        AIComBundle third = new AIComBundle();
        Room room = builder.setName("Occupancy Room").addPrebuiltNPC(first.npc).addPrebuiltNPC(second.npc)
                .addPrebuiltNPC(third.npc).build();
        Bed bed = new Bed(room, Bed.Builder.getInstance().setCapacity(2).setSleepSeconds(2));
        room.addItem(bed);

        GameEvent out = bed.doUseAction(first.npc);
        Truth.assertThat(out.toString()).contains("You are now in the bed");
        Truth.assertThat(bed.getOccupancy()).isEqualTo(1);

        out = bed.doUseAction(second.npc);
        Truth.assertThat(out.toString()).contains("You are now in the bed");
        Truth.assertThat(bed.getOccupancy()).isEqualTo(2);

        out = bed.doUseAction(third.npc);
        Truth.assertThat(out.toString()).doesNotContain("You are now in the bed");
        Truth.assertThat(bed.getOccupancy()).isEqualTo(2);

        // one gets out

        bed.removeCreature(first.npc);
        Truth.assertThat(bed.getOccupancy()).isEqualTo(1);

        out = bed.doUseAction(third.npc);
        Truth.assertThat(out.toString()).contains("You are now in the bed");
        Truth.assertThat(bed.getOccupancy()).isEqualTo(2);

    }

    @Test
    void testBedTime() {
        AIComBundle first = new AIComBundle();
        Room room = builder.setName("Sleeping Room").addPrebuiltNPC(first.npc).build();
        Bed bed = new Bed(room, Bed.Builder.getInstance().setCapacity(1).setSleepSeconds(1));

        GameEvent out = bed.doUseAction(first.npc);
        Truth.assertThat(out.toString()).contains("You are now in the bed");
        Truth.assertThat(bed.getOccupancy()).isEqualTo(1);

        Mockito.verify(first.sssb, Mockito.after(bed.sleepSeconds * 1000).atMostOnce())
                .send(Mockito.argThat(new MessageMatcher("You slept")));
    }
}
