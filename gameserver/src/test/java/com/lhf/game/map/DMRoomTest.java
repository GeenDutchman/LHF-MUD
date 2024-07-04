package com.lhf.game.map;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.item.concrete.InteractDoor;
import com.lhf.game.map.DMRoom.DMRoomBuilder;
import com.lhf.game.map.Dungeon.DungeonBuilder;
import com.lhf.game.map.Room.RoomBuilder;
import com.lhf.messages.GameEventType;
import com.lhf.messages.MessageMatcher;

public class DMRoomTest {
    @Test
    void testDMRoomAtlas() {
        final String controlRoomName = "Control Room";
        final String landAName = "First Dungeon";
        final String landARoomAName = "Room A";
        final String landBName = "Second Dungeon";
        final String landBRoomBName = "Room B";
        final InteractDoor door = new InteractDoor("Swirling Portal", "A swirl of purple beckons...", landBName,
                landBRoomBName);
        DMRoomBuilder builder = DMRoomBuilder.getInstance().setName(controlRoomName);
        DungeonBuilder landA = DungeonBuilder.newInstance().setName(landAName)
                .addStartingRoom(RoomBuilder.getInstance().setName(landARoomAName).addItem(door));
        DungeonBuilder landB = DungeonBuilder.newInstance().setName(landBName)
                .addStartingRoom(RoomBuilder.getInstance().setName(landBRoomBName));
        builder.getLandBuilders().addMember(landA);
        builder.getLandBuilders().addMember(landB);

        final DMRoom built = builder.quickBuild(null, null, null);
        AreaSubject.assertThat(built).isNotNull();
        // AtlasSubject.assertThat(built.getLands()).asMermaid().contains(landBRoomBName);
        // AtlasSubject.assertThat(built.getLands()).asMermaid().contains(landARoomAName);
        LandSubject.assertThat(built.getFirstLand()).startingArea().asItemContainer().containsExactly(door);

        AIComBundle bundle = new AIComBundle();
        LandSubject.assertThat(built.getFirstLand()).creatureIsAdded(bundle.getNPC());
        Mockito.verify(bundle.sssb, Mockito.timeout(1000)).send(
                Mockito.argThat(new MessageMatcher(GameEventType.SEE, List.of(door.getName(), landARoomAName), null)));

        bundle.brain.ProcessString("INTERACT " + door.getName());
        Mockito.verify(bundle.sssb, Mockito.timeout(1000))
                .send(Mockito.argThat(new MessageMatcher(GameEventType.SEE, landBRoomBName)));
    }
}
