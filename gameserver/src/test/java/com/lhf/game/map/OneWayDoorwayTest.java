package com.lhf.game.map;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.map.Dungeon.DungeonBuilder;
import com.lhf.game.map.Room.RoomBuilder;
import com.lhf.messages.MessageMatcher;
import com.lhf.messages.GameEventType;

public class OneWayDoorwayTest {
        @Test
        void testCanTraverse() {
                DungeonBuilder builder = DungeonBuilder.newInstance();
                RoomBuilder roomA = RoomBuilder.getInstance().setName("roomA");
                RoomBuilder roomB = RoomBuilder.getInstance().setName("roomB");
                builder.addStartingRoom(roomA);
                builder.connectRoomOneWay(roomA, Directions.EAST, roomB);
                Dungeon dungeon = builder.quickBuild(null, null);
                System.out.println(dungeon.toMermaid(false));

                AIComBundle bundle = new AIComBundle();
                dungeon.addCreature(bundle.getNPC());
                Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                                .send(Mockito.argThat(new MessageMatcher(GameEventType.SEE,
                                                List.of(roomA.getName(), Directions.EAST.toString().toLowerCase()),
                                                null)));

                bundle.brain.ProcessString("go east");
                Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                                .send(Mockito.argThat(new MessageMatcher(GameEventType.SEE,
                                                List.of(roomB.getName()), null)));

                bundle.brain.ProcessString("go west");
                Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                                .send(Mockito.argThat(new MessageMatcher(GameEventType.BAD_GO, "wall")));
        }
}
