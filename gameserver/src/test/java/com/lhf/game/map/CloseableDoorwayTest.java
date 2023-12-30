package com.lhf.game.map;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.truth.Truth;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.map.Atlas.AtlasMappingItem;
import com.lhf.game.map.Atlas.TargetedTester;
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
                builder.connectRoom(closeable, roomBBuilder, Directions.EAST, roomABuilder);
                Dungeon dungeon = builder.quickBuild(null, null);
                System.out.println(dungeon.toMermaid(false));

                Area roomA = dungeon.getAreaByName("roomA").orElse(null);
                Area roomB = dungeon.getAreaByName("roomB").orElse(null);
                Truth.assertThat(roomA).isNotNull();
                Truth.assertThat(roomB).isNotNull();

                AtlasMappingItem<Area, UUID> aMappingItem = dungeon.getAtlas().getAtlasMappingItem(roomA);
                AtlasMappingItem<Area, UUID> bMappingItem = dungeon.getAtlas().getAtlasMappingItem(roomB);

                Truth.assertThat(bMappingItem.getAvailableDirections()).contains(Directions.EAST);
                TargetedTester<UUID> aUUID = bMappingItem.getDirections().get(Directions.EAST);
                Truth.assertThat(aUUID).isNotNull();
                Truth.assertThat(aUUID.getTargetId()).isEqualTo(roomA.getUuid());
                Truth.assertThat(aUUID.getPredicate()).isEqualTo(closeable);

                Truth.assertThat(aMappingItem.getAvailableDirections()).contains(Directions.WEST);
                TargetedTester<UUID> bUUID = aMappingItem.getDirections().get(Directions.WEST);
                Truth.assertThat(bUUID).isNotNull();
                Truth.assertThat(bUUID.getTargetId()).isEqualTo(roomB.getUuid());
                Truth.assertThat(bUUID.getPredicate()).isEqualTo(closeable);

                Truth.assertThat(closeable.isOpen()).isFalse();

                AIComBundle bundle = new AIComBundle();
                roomA.addCreature(bundle.getNPC());
                Mockito.verify(bundle.sssb, Mockito.timeout(1000)).send(Mockito.argThat(
                                new MessageMatcher(GameEventType.SEE,
                                                List.of(roomA.getName(), Directions.WEST.toString().toLowerCase()),
                                                null)));

                bundle.brain.ProcessString("go west");
                Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                                .send(Mockito.argThat(new MessageMatcher(GameEventType.BAD_GO, "blocked")));

                closeable.open();
                Truth.assertThat(closeable.isOpen()).isTrue();

                bundle.brain.ProcessString("go west");
                Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                                .send(Mockito.argThat(new MessageMatcher(GameEventType.SEE,
                                                List.of(roomB.getName(), Directions.EAST.toString().toLowerCase()),
                                                null)));

                bundle.brain.ProcessString("go east");
                Mockito.verify(bundle.sssb, Mockito.timeout(1000).times(2))
                                .send(Mockito.argThat(new MessageMatcher(GameEventType.SEE,
                                                List.of(roomA.getName(), Directions.WEST.toString().toLowerCase()),
                                                null)));

                closeable.close();
                Truth.assertThat(closeable.isOpen()).isFalse();

                bundle.brain.ProcessString("go west");
                Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                                .send(Mockito.argThat(new MessageMatcher(GameEventType.SEE,
                                                List.of(roomB.getName(), Directions.EAST.toString().toLowerCase()),
                                                null)));
        }
}
