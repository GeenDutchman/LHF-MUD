package com.lhf.game.item.concrete;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.creature.intelligence.handlers.LewdAIHandler;
import com.lhf.game.map.Room;
import com.lhf.game.map.RoomBuilder;

public class LewdBedTest {

    protected RoomBuilder builder = RoomBuilder.getInstance();

    @Test
    void testSolo() {
        AIComBundle first = new AIComBundle();
        Room room = this.builder.setName("Solo").addCreature(first.npc).build();
        LewdBed bed = new LewdBed(room, 1, 30);
        room.addItem(bed);

        bed.addCreature(first.npc);

        Truth.assertThat(bed.handleEmptyJoin(first.npc)).isTrue();
        Truth.assertThat(first.read()).contains("meant to be shared");

        Truth.assertThat(bed.handlePopulatedJoin(first.npc, null)).isTrue();
        Truth.assertThat(first.read()).contains("meant to be shared");
    }

    @Test
    void testOnePartner() {
        AIComBundle first = new AIComBundle();
        AIComBundle second = new AIComBundle();

        first.brain.addHandler(new LewdAIHandler(Set.of(second.npc)));
        second.brain.addHandler(new LewdAIHandler(Set.of(first.npc)));

        Room room = this.builder.setName("Pair").addCreature(first.npc).addCreature(second.npc).build();
        LewdBed bed = new LewdBed(room, 1, 30);
        room.addItem(bed);

        bed.addCreature(first.npc);
        bed.addCreature(second.npc);

        first.read();
        second.read();

        Truth.assertThat(bed.handlePopulatedJoin(first.npc, Set.of(second.npc.getName()))).isTrue();
        first.read();
        second.read();
    }
}
