package com.lhf.game.map;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.truth.Truth;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.events.messages.MessageMatcher;
import com.lhf.game.events.messages.OutMessageType;
import com.lhf.game.item.concrete.LockKey;
import com.lhf.game.map.DoorwayFactory.DoorwayType;
import com.lhf.game.map.Land.AreaDirectionalLinks;
import com.lhf.game.map.Room.RoomBuilder;

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

                AreaDirectionalLinks exits = dungeon.getAreaDirectionalLinks(roomB);
                Doorway genericDoor = exits.getExits().get(Directions.EAST);
                Truth.assertThat(genericDoor.getRoomAccross(roomA.getUuid())).isEqualTo(roomB.getUuid());
                Truth.assertThat(genericDoor.getRoomAccross(roomB.getUuid())).isEqualTo(roomA.getUuid());
                Truth.assertThat(genericDoor.getType()).isEqualTo(DoorwayType.KEYED);
                KeyedDoorway doorway = (KeyedDoorway) genericDoor;
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

                LockKey key = doorway.generateKey();
                bundle.npc.addItem(key);
                Truth.assertThat(bundle.npc.hasItem(key.getName()));
                Truth.assertThat(LockKey.generateKeyName(doorway.getDoorwayUuid())).isEqualTo(key.getName());

                bundle.brain.ProcessString("go west");
                Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                                .send(Mockito.argThat(new MessageMatcher(OutMessageType.SEE,
                                                List.of(roomB.getName(), Directions.EAST.toString().toLowerCase()),
                                                null)));

                Truth.assertThat(bundle.npc.hasItem(key.getName())).isFalse();
                Truth.assertThat(doorway.isOpen()).isTrue();

                bundle.brain.ProcessString("go east");
                Mockito.verify(bundle.sssb, Mockito.timeout(1000).times(2))
                                .send(Mockito.argThat(new MessageMatcher(OutMessageType.SEE,
                                                List.of(roomA.getName(), Directions.WEST.toString().toLowerCase()),
                                                null)));

        }
}
