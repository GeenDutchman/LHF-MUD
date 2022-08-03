package com.lhf.game.map;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.map.DoorwayFactory.DoorwayType;
import com.lhf.game.map.Dungeon.RoomAndDirs;

public class CloseableDoorwayTest {
    @Test
    void testCanTraverse() {
        DungeonBuilder builder = DungeonBuilder.newInstance();
        RoomBuilder rBuilder = RoomBuilder.getInstance();
        Room roomA = rBuilder.setName("roomA").build();
        Room roomB = rBuilder.setName("roomB").build();
        builder.addStartingRoom(roomA);
        builder.connectRoom(DoorwayType.CLOSEABLE, roomB, Directions.EAST, roomA);
        Dungeon dungeon = builder.build();
        System.out.println(dungeon.toMermaid(false));

        RoomAndDirs exits = dungeon.getRoomExits(roomB);
        Doorway genericDoor = exits.exits.get(Directions.EAST);
        Truth.assertThat(genericDoor.getRoomAccross(roomA.getUuid())).isEqualTo(roomB.getUuid());
        Truth.assertThat(genericDoor.getRoomAccross(roomB.getUuid())).isEqualTo(roomA.getUuid());
        Truth.assertThat(genericDoor.getType()).isEqualTo(DoorwayType.CLOSEABLE);
        CloseableDoorway doorway = (CloseableDoorway) genericDoor;
        Truth.assertThat(doorway.isOpen()).isFalse();

        AIComBundle bundle = new AIComBundle();
        roomA.addCreature(bundle.npc);
        String seen = bundle.read();
        Truth.assertThat(seen).contains(roomA.getName());
        Truth.assertThat(seen).ignoringCase().contains(Directions.WEST.toString());
        bundle.clear();

        bundle.brain.ProcessString("go west");
        seen = bundle.read();
        Truth.assertThat(seen).ignoringCase().contains("blocked");
        bundle.clear();

        doorway.open();
        Truth.assertThat(doorway.isOpen()).isTrue();

        bundle.brain.ProcessString("go west");
        seen = bundle.read();
        Truth.assertThat(seen).contains(roomB.getName());
        Truth.assertThat(seen).ignoringCase().contains(Directions.EAST.toString());
        bundle.clear();

        bundle.brain.ProcessString("go east");
        seen = bundle.read();
        Truth.assertThat(seen).contains(roomA.getName());
        Truth.assertThat(seen).ignoringCase().contains(Directions.WEST.toString());
        bundle.clear();

        doorway.close();
        Truth.assertThat(doorway.isOpen()).isFalse();

        bundle.brain.ProcessString("go west");
        seen = bundle.read();
        Truth.assertThat(seen).ignoringCase().contains("blocked");
        bundle.clear();
    }
}
