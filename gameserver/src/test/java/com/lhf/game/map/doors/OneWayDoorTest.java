package com.lhf.game.map.doors;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.map.Directions;
import com.lhf.game.map.Dungeon;
import com.lhf.game.map.DungeonBuilder;
import com.lhf.game.map.Room;
import com.lhf.game.map.RoomBuilder;

public class OneWayDoorTest {
    private class TestDungeon {
        public Dungeon d;
        public Room roomA; // starting room
        public Room roomB;
        // public Doorway door;
        public AIComBundle comBundle;

        public TestDungeon() {
            DungeonBuilder DBuilder = DungeonBuilder.newInstance();
            RoomBuilder firstRoomBuilder = RoomBuilder.getInstance();
            firstRoomBuilder.setName("RoomA");
            this.roomA = firstRoomBuilder.build();
            RoomBuilder othRoomBuilder = RoomBuilder.getInstance();
            othRoomBuilder.setName("RoomB");
            this.roomB = othRoomBuilder.build();
            DBuilder.addStartingRoom(this.roomA);
            DBuilder.addSecretDoor(this.roomA, Directions.EAST, this.roomB);
            this.comBundle = new AIComBundle();
            this.d = DBuilder.build();
        }
    }

    // @Test
    // void testGetRoomA() {
    // // TODO: MOVE GO UP
    // }

    // @Test
    // void testGetRoomB() {

    // }

    @Test
    void testTraverse() {
        TestDungeon dungeon = new TestDungeon();
        dungeon.roomB.addCreature(dungeon.comBundle.npc);
        String seen = dungeon.comBundle.read();
        Truth.assertThat(seen).contains(dungeon.roomB.getName());
        Truth.assertThat(seen).contains(Directions.EAST.toString());
        dungeon.comBundle.clear();
        dungeon.comBundle.brain.ProcessString("go east");
        seen = dungeon.comBundle.read();
        Truth.assertThat(seen).contains(dungeon.roomA.getName());
        Truth.assertThat(seen).doesNotContain(Directions.WEST.toString());
        dungeon.comBundle.clear();
        dungeon.comBundle.brain.ProcessString("go west");
        Truth.assertThat(dungeon.comBundle.read()).contains("wall");
    }
}
