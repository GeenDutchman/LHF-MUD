package com.lhf.game.map;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.events.messages.MessageMatcher;
import com.lhf.game.events.messages.OutMessageType;
import com.lhf.game.map.Room.RoomBuilder;

public class DoorwayTest {
        @Test
        void testCanTraverse() {
                DungeonBuilder builder = DungeonBuilder.newInstance();
                RoomBuilder rBuilder = RoomBuilder.getInstance();
                Room roomA = rBuilder.setName("roomA").build();
                Room roomB = rBuilder.setName("roomB").build();
                builder.addStartingRoom(roomA);
                builder.connectRoom(roomB, Directions.EAST, roomA);
                Dungeon dungeon = builder.build();
                System.out.println(dungeon.toMermaid(false));

                AIComBundle bundle = new AIComBundle();
                roomA.addCreature(bundle.npc);

                MessageMatcher hasWest = new MessageMatcher(OutMessageType.SEE,
                                List.of(roomA.getName(), Directions.WEST.toString().toLowerCase()), null);

                Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                                .send(Mockito.argThat(hasWest));

                bundle.brain.ProcessString("go west");
                Mockito.verify(bundle.sssb, Mockito.timeout(1000)).send(Mockito.argThat(
                                new MessageMatcher(OutMessageType.SEE,
                                                List.of(roomB.getName(), Directions.EAST.toString().toLowerCase()),
                                                null)));

                bundle.brain.ProcessString("go east");
                Mockito.verify(bundle.sssb, Mockito.timeout(1000).times(2))
                                .send(Mockito.argThat(hasWest));

        }
}
