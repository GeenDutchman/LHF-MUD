package com.lhf.game.map;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.events.messages.MessageMatcher;
import com.lhf.game.events.messages.OutMessageType;
import com.lhf.game.map.Room.RoomBuilder;

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
                Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                                .send(Mockito.argThat(new MessageMatcher(OutMessageType.SEE,
                                                List.of(roomB.getName(), Directions.EAST.toString().toLowerCase()),
                                                null)));

                bundle.brain.ProcessString("go east");
                Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                                .send(Mockito.argThat(new MessageMatcher(OutMessageType.SEE,
                                                List.of(roomA.getName()), null)));

                bundle.brain.ProcessString("go west");
                Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                                .send(Mockito.argThat(new MessageMatcher(OutMessageType.BAD_GO, "wall")));
        }
}
