package com.lhf.game.map;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.truth.Truth;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.map.DoorwayFactory.DoorwayType;
import com.lhf.game.map.Dungeon.AreaAndDirs;
import com.lhf.game.map.Land.AreaDirectionalLinks;
import com.lhf.game.map.Room.RoomBuilder;
import com.lhf.messages.MessageMatcher;
import com.lhf.messages.OutMessageType;

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

                AreaDirectionalLinks exits = dungeon.getAreaDirectionalLinks(roomB);
                Doorway genericDoor = exits.getExits().get(Directions.EAST);
                Truth.assertThat(genericDoor.getRoomAccross(roomA.getUuid())).isEqualTo(roomB.getUuid());
                Truth.assertThat(genericDoor.getRoomAccross(roomB.getUuid())).isEqualTo(roomA.getUuid());
                Truth.assertThat(genericDoor.getType()).isEqualTo(DoorwayType.CLOSEABLE);
                CloseableDoorway doorway = (CloseableDoorway) genericDoor;
                Truth.assertThat(doorway.isOpen()).isFalse();

                AIComBundle bundle = new AIComBundle();
                roomA.addCreature(bundle.npc);
                Mockito.verify(bundle.sssb, Mockito.timeout(1000)).send(Mockito.argThat(
                                new MessageMatcher(OutMessageType.SEE,
                                                List.of(roomA.getName(), Directions.WEST.toString().toLowerCase()),
                                                null)));

                bundle.brain.ProcessString("go west");
                Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                                .send(Mockito.argThat(new MessageMatcher(OutMessageType.BAD_GO, "blocked")));

                doorway.open();
                Truth.assertThat(doorway.isOpen()).isTrue();

                bundle.brain.ProcessString("go west");
                Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                                .send(Mockito.argThat(new MessageMatcher(OutMessageType.SEE,
                                                List.of(roomB.getName(), Directions.EAST.toString().toLowerCase()),
                                                null)));

                bundle.brain.ProcessString("go east");
                Mockito.verify(bundle.sssb, Mockito.timeout(1000).times(2))
                                .send(Mockito.argThat(new MessageMatcher(OutMessageType.SEE,
                                                List.of(roomA.getName(), Directions.WEST.toString().toLowerCase()),
                                                null)));

                doorway.close();
                Truth.assertThat(doorway.isOpen()).isFalse();

                bundle.brain.ProcessString("go west");
                Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                                .send(Mockito.argThat(new MessageMatcher(OutMessageType.SEE,
                                                List.of(roomB.getName(), Directions.EAST.toString().toLowerCase()),
                                                null)));
        }
}
