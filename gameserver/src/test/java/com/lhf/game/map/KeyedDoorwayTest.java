package com.lhf.game.map;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.item.concrete.LockKey;
import com.lhf.game.map.DoorwayFactory.DoorwayType;
import com.lhf.game.map.Dungeon.RoomAndDirs;

public class KeyedDoorwayTest {
    @Test
    void testCanTraverse() {
        DungeonBuilder builder = DungeonBuilder.newInstance();
        RoomBuilder rBuilder = RoomBuilder.getInstance();
        Room roomA = rBuilder.setName("roomA").build();
        Room roomB = rBuilder.setName("roomB").build();
        builder.addStartingRoom(roomA);
        builder.connectRoom(DoorwayType.KEYED, roomB, Directions.EAST, roomA);
        Dungeon dungeon = builder.build();
        System.out.println(dungeon.toMermaid(false));

        RoomAndDirs exits = dungeon.getRoomExits(roomB);
        Doorway genericDoor = exits.exits.get(Directions.EAST);
        Truth.assertThat(genericDoor.getRoomAccross(roomA.getUuid())).isEqualTo(roomB.getUuid());
        Truth.assertThat(genericDoor.getRoomAccross(roomB.getUuid())).isEqualTo(roomA.getUuid());
        Truth.assertThat(genericDoor.getType()).isEqualTo(DoorwayType.KEYED);
        KeyedDoorway doorway = (KeyedDoorway) genericDoor;
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

        LockKey key = doorway.generateKey();
        bundle.npc.addItem(key);
        Truth.assertThat(bundle.npc.hasItem(key.getName()));
        Truth.assertThat(LockKey.generateKeyName(doorway.getDoorwayUuid())).isEqualTo(key.getName());

        bundle.brain.ProcessString("go west");
        seen = bundle.read();
        Truth.assertThat(seen).contains(roomB.getName());
        Truth.assertThat(seen).ignoringCase().contains(Directions.EAST.toString());
        bundle.clear();

        Truth.assertThat(bundle.npc.hasItem(key.getName())).isFalse();
        Truth.assertThat(doorway.isOpen()).isTrue();

        bundle.brain.ProcessString("go east");
        seen = bundle.read();
        Truth.assertThat(seen).contains(roomA.getName());
        Truth.assertThat(seen).ignoringCase().contains(Directions.WEST.toString());
        bundle.clear();

    }
}
