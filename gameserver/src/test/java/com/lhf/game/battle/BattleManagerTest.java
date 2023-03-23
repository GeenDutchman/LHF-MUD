package com.lhf.game.battle;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.truth.Truth;
import com.lhf.game.creature.Monster;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.map.Area;
import com.lhf.messages.MessageMatcher;
import com.lhf.messages.OutMessageType;

@ExtendWith(MockitoExtension.class)
public class BattleManagerTest {
    @Test
    void testSimpleBattle() {
        Monster monster = Mockito.mock(Monster.class);
        NonPlayerCharacter npc = Mockito.mock(NonPlayerCharacter.class);
        Area area = Mockito.mock(Area.class);

        Mockito.when(monster.getName()).thenReturn("Monster");
        // Mockito.when(monster.getColorTaggedName()).thenCallRealMethod();
        Mockito.when(npc.getName()).thenReturn("NPC");
        Mockito.when(npc.getColorTaggedName()).thenCallRealMethod();
        // Mockito.when(area.getCreatures()).thenReturn(List.of(monster, npc));

        Initiative.Builder initiative = FIFOInitiative.Builder.getInstance();
        Truth.assertThat(initiative.addCreature(npc)).isTrue();
        Truth.assertThat(initiative.addCreature(monster)).isTrue();

        BattleManager battleManager = BattleManager.Builder.getInstance().setInitiativeBuilder(initiative).Build(area);

        battleManager.startBattle(monster, List.of(npc));
        MessageMatcher startBattle = new MessageMatcher(OutMessageType.START_FIGHT).setPrint(true, null);
        MessageMatcher turnMessage = new MessageMatcher(OutMessageType.BATTLE_TURN).setPrint(true, null);
        Mockito.verify(npc, Mockito.timeout(1000).atLeastOnce()).sendMsg(Mockito.argThat(startBattle));

        Mockito.verify(npc, Mockito.timeout(1000).atLeastOnce()).sendMsg(Mockito.argThat(turnMessage));

        // Truth.assertThat(false).isTrue();
    }
}
