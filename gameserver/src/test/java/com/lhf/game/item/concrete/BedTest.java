package com.lhf.game.item.concrete;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.map.Room;
import com.lhf.game.map.RoomBuilder;
import com.lhf.messages.out.OutMessage;

public class BedTest {

    protected RoomBuilder builder = RoomBuilder.getInstance();

    @Test
    void testGetCapacity() {
        Room room = builder.setName("Capacity Room").build();
        int capacity = 2;
        Bed bed = new Bed(room, capacity, 0);
        Truth.assertThat(bed.getCapacity()).isEqualTo(capacity);
    }

    @Test
    void testGetOccupancy() {
        AIComBundle first = new AIComBundle();
        AIComBundle second = new AIComBundle();
        AIComBundle third = new AIComBundle();
        Room room = builder.setName("Occupancy Room").addCreature(first.npc).addCreature(second.npc)
                .addCreature(third.npc).build();
        Bed bed = new Bed(room, 2, 2);
        room.addItem(bed);

        OutMessage out = bed.doUseAction(first.npc);
        Truth.assertThat(out.toString()).contains("You are now in the bed");
        Truth.assertThat(bed.getOccupancy()).isEqualTo(1);

        out = bed.doUseAction(second.npc);
        Truth.assertThat(out.toString()).contains("You are now in the bed");
        Truth.assertThat(bed.getOccupancy()).isEqualTo(2);

        out = bed.doUseAction(third.npc);
        Truth.assertThat(out.toString()).doesNotContain("You are now in the bed");
        Truth.assertThat(bed.getOccupancy()).isEqualTo(2);

        // one gets out

        bed.remove(first.npc);
        Truth.assertThat(bed.getOccupancy()).isEqualTo(1);

        out = bed.doUseAction(third.npc);
        Truth.assertThat(out.toString()).contains("You are now in the bed");
        Truth.assertThat(bed.getOccupancy()).isEqualTo(2);

    }

    @Test
    void testBedTime() {
        AIComBundle first = new AIComBundle();
        Room room = builder.setName("Sleeping Room").addCreature(first.npc).build();
        Bed bed = new Bed(room, 1, 1);

        OutMessage out = bed.doUseAction(first.npc);
        Truth.assertThat(out.toString()).contains("You are now in the bed");
        Truth.assertThat(bed.getOccupancy()).isEqualTo(1);

        try {
            TimeUnit.SECONDS.sleep(bed.sleepSeconds + 1);
        } catch (InterruptedException e) {
            fail(e);
        }
        Truth.assertThat(first.read()).ignoringCase().contains("You slept");
    }
}
