package com.lhf.game.map;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.truth.Truth;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.map.Dungeon.DungeonBuilder;
import com.lhf.game.map.Room.RoomBuilder;
import com.lhf.messages.MessageMatcher;
import com.lhf.messages.GameEventType;

public class DoorwayTest {
        @Test
        void testCanTraverse() {
                DungeonBuilder builder = DungeonBuilder.newInstance();
                RoomBuilder roomABuilder = RoomBuilder.getInstance().setName("roomA");
                RoomBuilder roomBBuilder = RoomBuilder.getInstance().setName("roomB");
                builder.addStartingRoom(roomABuilder);
                builder.connectRoom(roomBBuilder, Directions.EAST, roomABuilder);
                Dungeon dungeon = builder.quickBuild(null, null);
                System.out.println(dungeon.toMermaid(false));

                Area roomA = dungeon.getAreaByName("roomA").orElse(null);
                Area roomB = dungeon.getAreaByName("roomB").orElse(null);
                Truth.assertThat(roomA).isNotNull();
                Truth.assertThat(roomB).isNotNull();

                AIComBundle bundle = new AIComBundle();
                roomA.addCreature(bundle.npc);

                MessageMatcher hasWest = new MessageMatcher(GameEventType.SEE,
                                List.of(roomA.getName(), Directions.WEST.toString().toLowerCase()), null);

                Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                                .send(Mockito.argThat(hasWest));

                bundle.brain.ProcessString("go west");
                Mockito.verify(bundle.sssb, Mockito.timeout(1000)).send(Mockito.argThat(
                                new MessageMatcher(GameEventType.SEE,
                                                List.of(roomB.getName(), Directions.EAST.toString().toLowerCase()),
                                                null)));

                bundle.brain.ProcessString("go east");
                Mockito.verify(bundle.sssb, Mockito.timeout(1000).times(2))
                                .send(Mockito.argThat(hasWest));

        }
}
