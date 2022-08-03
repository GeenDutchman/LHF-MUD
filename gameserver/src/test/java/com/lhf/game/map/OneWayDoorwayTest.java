package com.lhf.game.map;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.creature.intelligence.AIComBundle;

public class OneWayDoorwayTest {
    @Test
    void testCanTraverse() {
        DungeonBuilder builder = DungeonBuilder.newInstance();
        RoomBuilder rBuilder = RoomBuilder.getInstance();
        Room roomA = rBuilder.setName("roomA").build();
        Room roomB = rBuilder.setName("roomB").build();
        builder.addStartingRoom(roomA);
        builder.connectRoomOneWay(roomB, Directions.EAST, roomA);
        Dungeon dungeon = builder.build();
        System.out.println(dungeon.toMermaid(false));

        AIComBundle bundle = new AIComBundle();
        roomB.addCreature(bundle.npc);
        String seen = bundle.read();
        Truth.assertThat(seen).contains(roomB.getName());
        Truth.assertThat(seen).ignoringCase().contains(Directions.EAST.toString());
        bundle.clear();

        bundle.brain.ProcessString("go east");
        seen = bundle.read();
        Truth.assertThat(seen).contains(roomA.toString());
        Truth.assertThat(seen).ignoringCase().doesNotContain(Directions.WEST.toString());
        bundle.clear();

        bundle.brain.ProcessString("go west");
        seen = bundle.read();
        Truth.assertThat(seen).ignoringCase().contains("wall");
    }
}
