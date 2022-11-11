package com.lhf.game.item.concrete;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.map.Room;
import com.lhf.game.map.RoomBuilder;

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

    }

    @Test
    void testRemove() {

    }
}
