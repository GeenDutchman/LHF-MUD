package com.lhf.game.map;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.truth.Truth;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.map.Dungeon.DungeonBuilder;
import com.lhf.game.map.Room.RoomBuilder;
import com.lhf.messages.GameEventType;
import com.lhf.messages.MessageMatcher;

public class CloseableDoorwayTest {
    @Test
    void testCanTraverse() {
        DungeonBuilder builder = DungeonBuilder.newInstance();
        RoomBuilder roomABuilder = RoomBuilder.getInstance().setName("roomA");
        RoomBuilder roomBBuilder = RoomBuilder.getInstance().setName("roomB");
        builder.addStartingRoom(roomABuilder);
        CloseableDoorway closeable = new CloseableDoorway();
        builder.connectRoom(roomBBuilder, Directions.EAST, roomABuilder, closeable);
        Dungeon dungeon = builder.quickBuild(null, null);
        System.out.println(dungeon.toMermaid(false));

        Area roomA = dungeon.getAreaByName("roomA").orElse(null);
        Area roomB = dungeon.getAreaByName("roomB").orElse(null);
        Truth.assertThat(roomA).isNotNull();
        Truth.assertThat(roomA.getUuid()).isNotNull();
        Truth.assertThat(roomB).isNotNull();
        Truth.assertThat(roomB.getUuid()).isNotNull();

        Truth.assertThat(dungeon.getAtlas().getLinksForMember(roomB.getUuid())).containsExactly(Directions.EAST);
        Truth.assertThat(dungeon.getAtlas().getLinkTypeBetween(roomB.getUuid(), roomA.getUuid()))
                .isEqualTo(Directions.EAST);
        Truth.assertThat(dungeon.getAtlas().getTargetFromMember(roomB.getUuid(), Directions.EAST))
                .isEqualTo(roomA.getUuid());
        Truth.assertThat(dungeon.getAtlas().getTraversalTestFromMember(roomB.getUuid(), Directions.EAST))
                .isEqualTo(closeable);

        Truth.assertThat(dungeon.getAtlas().getLinksForMember(roomA.getUuid())).containsExactly(Directions.WEST);
        Truth.assertThat(dungeon.getAtlas().getLinkTypeBetween(roomA.getUuid(), roomB.getUuid()))
                .isEqualTo(Directions.WEST);
        Truth.assertThat(dungeon.getAtlas().getTargetFromMember(roomA.getUuid(), Directions.WEST))
                .isEqualTo(roomB.getUuid());
        Truth.assertThat(dungeon.getAtlas().getTraversalTestFromMember(roomA.getUuid(), Directions.WEST))
                .isEqualTo(closeable);

        Truth.assertThat(closeable.isOpen()).isFalse();

        AIComBundle bundle = new AIComBundle();
        roomA.addCreature(bundle.getNPC());
        Mockito.verify(bundle.sssb, Mockito.timeout(1000)).send(Mockito.argThat(new MessageMatcher(GameEventType.SEE,
                List.of(roomA.getName(), Directions.WEST.toString().toLowerCase()), null)));

        bundle.brain.ProcessString("go west");
        Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                .send(Mockito.argThat(new MessageMatcher(GameEventType.BAD_GO, "blocked")));

        closeable.open();
        Truth.assertThat(closeable.isOpen()).isTrue();

        bundle.brain.ProcessString("go west");
        Mockito.verify(bundle.sssb, Mockito.timeout(1000)).send(Mockito.argThat(new MessageMatcher(GameEventType.SEE,
                List.of(roomB.getName(), Directions.EAST.toString().toLowerCase()), null)));

        bundle.brain.ProcessString("go east");
        Mockito.verify(bundle.sssb, Mockito.timeout(1000).times(2))
                .send(Mockito.argThat(new MessageMatcher(GameEventType.SEE,
                        List.of(roomA.getName(), Directions.WEST.toString().toLowerCase()), null)));

        closeable.close();
        Truth.assertThat(closeable.isOpen()).isFalse();

        bundle.brain.ProcessString("go west");
        Mockito.verify(bundle.sssb, Mockito.timeout(1000)).send(Mockito.argThat(new MessageMatcher(GameEventType.SEE,
                List.of(roomB.getName(), Directions.EAST.toString().toLowerCase()), null)));
    }
}
