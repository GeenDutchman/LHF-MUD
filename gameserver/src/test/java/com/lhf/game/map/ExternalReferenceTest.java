package com.lhf.game.map;

import static com.lhf.game.map.LandSubject.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.lhf.game.Atlas.AtlasException;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.map.Dungeon.DungeonBuilder;
import com.lhf.game.map.Room.RoomBuilder;
import com.lhf.messages.GameEventType;
import com.lhf.messages.MessageMatcher;

public class ExternalReferenceTest {
    @Test
    void testCanTraverseSameDungeon() throws AtlasException {
        final String dungeonName = "My Dungeon";
        final String roomAName = "roomA";
        final String roomBName = "roomB";
        DungeonBuilder builder = DungeonBuilder.newInstance();
        builder.setName(dungeonName);
        RoomBuilder roomABuilder = RoomBuilder.getInstance().setName(roomAName);
        RoomBuilder roomBBuilder = RoomBuilder.getInstance().setName(roomBName);
        builder.addStartingRoom(roomABuilder);
        builder.getAtlas().addMember(roomBBuilder);
        builder.getAtlas().connectOneWayExternally(roomABuilder, Directions.EAST, null, dungeonName, roomBName);

        Dungeon dungeon = builder.quickBuild(null, null);
        dungeon.getAtlas()
                .populateExternalReferences((dName, aName) -> dungeonName.equals(dName) && roomBName.equals(aName)
                        ? dungeon.getAreaByName(aName).orElse(null)
                        : null);

        System.out.println(dungeon.toMermaid(false));

        assertThat(dungeon).areaByName(roomAName).isPresent();
        assertThat(dungeon).areaByName(roomBName).isPresent();

        AIComBundle bundle = new AIComBundle();
        dungeon.addCreature(bundle.getNPC());
        Mockito.verify(bundle.sssb, Mockito.timeout(1000)).send(Mockito.argThat(new MessageMatcher(GameEventType.SEE,
                List.of(roomAName, Directions.EAST.toString().toLowerCase()), null)));

        bundle.brain.ProcessString("go east");
        Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                .send(Mockito.argThat(new MessageMatcher(GameEventType.SEE, List.of(roomBName), null)));

        bundle.brain.ProcessString("go west");
        Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                .send(Mockito.argThat(new MessageMatcher(GameEventType.BAD_GO, "wall")));
    }

    @Test
    void testCanTraverseDifferentDungeon() throws AtlasException {
        final String dungeonName = "My Dungeon";
        final String roomAName = "roomA";
        final String bDungeonName = "Their Dungeon";
        final String roomBName = "roomB";
        DungeonBuilder builderA = DungeonBuilder.newInstance();
        builderA.setName(dungeonName);
        DungeonBuilder builderB = DungeonBuilder.newInstance().setName(bDungeonName);
        RoomBuilder roomABuilder = RoomBuilder.getInstance().setName(roomAName);
        RoomBuilder roomBBuilder = RoomBuilder.getInstance().setName(roomBName);
        builderA.addStartingRoom(roomABuilder);
        builderB.addStartingRoom(roomBBuilder);
        builderA.getAtlas().connectOneWayExternally(roomABuilder, Directions.EAST, null, bDungeonName, roomBName);

        Dungeon dungeonA = builderA.quickBuild(null, null);
        Dungeon dungeonB = builderB.quickBuild(null, null);
        dungeonA.getAtlas()
                .populateExternalReferences((dName, aName) -> bDungeonName.equals(dName) && roomBName.equals(aName)
                        ? dungeonB.getAreaByName(aName).orElse(null)
                        : null);

        System.out.println(dungeonA.toMermaid(false));
        System.out.println(dungeonB.toMermaid(false));

        assertThat(dungeonA).areaByName(roomAName).isPresent();
        assertThat(dungeonA).areaByName(roomBName).isEmpty();
        assertThat(dungeonB).areaByName(roomBName).isPresent();
        assertThat(dungeonB).areaByName(roomAName).isEmpty();

        AIComBundle bundle = new AIComBundle();
        dungeonA.addCreature(bundle.getNPC());
        Mockito.verify(bundle.sssb, Mockito.timeout(1000)).send(Mockito.argThat(new MessageMatcher(GameEventType.SEE,
                List.of(roomAName, Directions.EAST.toString().toLowerCase()), null)));

        bundle.brain.ProcessString("go east");
        Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                .send(Mockito.argThat(new MessageMatcher(GameEventType.SEE, List.of(roomBName), null)));

        bundle.brain.ProcessString("go west");
        Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                .send(Mockito.argThat(new MessageMatcher(GameEventType.BAD_GO, "wall")));

        assertThat(dungeonB).hasCreature(bundle.getNPC());
        assertThat(dungeonA).doesNotHaveCreature(bundle.getNPC());
    }
}
