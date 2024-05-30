package com.lhf.game.map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.truth.Truth;
import com.lhf.game.creature.CreatureVisitor;
import com.lhf.game.creature.Player;
import com.lhf.messages.GameEventProcessor.GameEventProcessorID;

@ExtendWith(MockitoExtension.class)
public class InstancedAreaTest {
    @Test
    void testAddCreature() {
        InstancedArea.InstancedAreaBuilder builder = InstancedArea.getBuilder().setLimit(1);

        InstancedArea area = builder.build(null, null, null, null, false);

        final GameEventProcessorID id1 = new GameEventProcessorID();
        final Player player1 = Mockito.mock(Player.class);
        Mockito.when(player1.getName()).thenReturn("George");
        Mockito.doAnswer(invocation -> {
            CreatureVisitor visitor = invocation.getArgument(0);
            Player myself = (Player) invocation.getMock();
            visitor.visit(myself);
            return null;
        }).when(player1).acceptCreatureVisitor(Mockito.any(CreatureVisitor.class));
        Mockito.when(player1.getEventProcessorID()).thenReturn(id1);
        Mockito.when(player1.getAcceptHook()).thenReturn(
                event -> System.out.println(String.format("Player %s: %s", player1.getName(), event.toString())));

        final GameEventProcessorID id2 = new GameEventProcessorID();
        final Player player2 = Mockito.mock(Player.class);
        Mockito.when(player2.getName()).thenReturn("Mary");
        Mockito.doAnswer(invocation -> {
            CreatureVisitor visitor = invocation.getArgument(0);
            Player myself = (Player) invocation.getMock();
            visitor.visit(myself);
            return null;
        }).when(player2).acceptCreatureVisitor(Mockito.any(CreatureVisitor.class));
        Mockito.when(player2.getEventProcessorID()).thenReturn(id2);
        Mockito.when(player2.getAcceptHook()).thenReturn(
                event -> System.out.println(String.format("Player %s: %s", player2.getName(), event.toString())));

        final GameEventProcessorID id3 = new GameEventProcessorID();
        final Player player3 = Mockito.mock(Player.class);
        Mockito.when(player3.getName()).thenReturn("Beren");
        Mockito.doAnswer(invocation -> {
            CreatureVisitor visitor = invocation.getArgument(0);
            Player myself = (Player) invocation.getMock();
            visitor.visit(myself);
            return null;
        }).when(player3).acceptCreatureVisitor(Mockito.any(CreatureVisitor.class));
        Mockito.when(player3.getEventProcessorID()).thenReturn(id3);
        Mockito.when(player3.getAcceptHook()).thenReturn(
                event -> System.out.println(String.format("Player %s: %s", player3.getName(), event.toString())));

        Truth.assertWithMessage("Area is such: '%s'", area).that(area.addCreature(player1)).isTrue();
        Truth.assertWithMessage("Area is such: '%s'", area).that(area.addCreature(player2)).isTrue();
        Truth.assertWithMessage("Area is such: '%s'", area).that(area.addCreature(player3)).isTrue();
        Truth.assertWithMessage("Area is such: '%s'", area).that(area.getRooms()).hasSize(3);
        Truth.assertWithMessage("Area is such: '%s'", area).that(area.getCreatures()).containsExactly(player1, player2,
                player3);

        for (final Room room : area.getRooms()) {
            Truth.assertWithMessage("Sub area is such: %s", room).that(room.getCreatures()).hasSize(1);
            Truth.assertWithMessage("Sub area is such: %s", room).that(room.getCreatures()).containsAnyOf(player1,
                    player2, player3);
        }
    }
}
