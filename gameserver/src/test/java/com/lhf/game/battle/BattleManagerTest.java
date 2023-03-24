package com.lhf.game.battle;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.truth.Truth;
import com.lhf.game.battle.BattleManager.BattleManagerThread;
import com.lhf.game.creature.Monster;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.enums.CreatureFaction;
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

        Initiative.Builder initiative = FIFOInitiative.Builder.getInstance();
        Truth.assertThat(initiative.addCreature(npc)).isTrue();
        Truth.assertThat(initiative.addCreature(monster)).isTrue();

        BattleManager battleManager = BattleManager.Builder.getInstance().setInitiativeBuilder(initiative).Build(area);

        battleManager.startBattle(monster, List.of(npc));
        MessageMatcher startBattle = new MessageMatcher(OutMessageType.START_FIGHT);
        MessageMatcher turnMessage = new MessageMatcher(OutMessageType.BATTLE_TURN);
        Mockito.verify(npc, Mockito.timeout(1000).atLeastOnce()).sendMsg(Mockito.argThat(startBattle.ownedCopy("npc")));

        Mockito.verify(npc, Mockito.timeout(1000).atLeastOnce()).sendMsg(Mockito.argThat(turnMessage.ownedCopy("npc")));

    }

    @Test
    void testWaitTooLong() throws InterruptedException {
        Monster monster = Mockito.mock(Monster.class);
        NonPlayerCharacter npc = Mockito.mock(NonPlayerCharacter.class);
        Area area = Mockito.mock(Area.class);

        Mockito.when(monster.getName()).thenReturn("Monster");
        // Mockito.when(monster.getColorTaggedName()).thenCallRealMethod();
        Mockito.when(monster.isAlive()).thenReturn(true);
        Mockito.when(monster.getFaction()).thenReturn(CreatureFaction.RENEGADE);
        Mockito.when(npc.getName()).thenReturn("NPC");
        Mockito.when(npc.getColorTaggedName()).thenReturn("NPC");
        Mockito.when(npc.isAlive()).thenReturn(true);
        Mockito.when(npc.getFaction()).thenReturn(CreatureFaction.RENEGADE);

        Initiative.Builder initiative = FIFOInitiative.Builder.getInstance();
        Truth.assertThat(initiative.addCreature(npc)).isTrue();
        Truth.assertThat(initiative.addCreature(monster)).isTrue();

        BattleManager battleManager = BattleManager.Builder.getInstance().setInitiativeBuilder(initiative)
                .setUnit(TimeUnit.MICROSECONDS).Build(area);
        Truth.assertThat(battleManager.getCreatures()).hasSize(2);

        BattleManagerThread thread = battleManager.startBattle(monster, List.of(npc));

        MessageMatcher startBattle = new MessageMatcher(OutMessageType.START_FIGHT).setPrint(true);
        MessageMatcher turnMessage = new MessageMatcher(OutMessageType.BATTLE_TURN, "It is now your turn to fight!")
                .setPrint(true);
        Mockito.verify(npc, Mockito.timeout(1000).atLeastOnce()).sendMsg(Mockito.argThat(startBattle.ownedCopy("npc")));

        Mockito.verify(npc, Mockito.timeout(1000).times(battleManager.getMaxPokesPerAction() + 1))
                .sendMsg(Mockito.argThat(turnMessage.ownedCopy("npc")));

        Mockito.verify(monster, Mockito.timeout(1000).atLeastOnce())
                .sendMsg(Mockito.argThat(turnMessage.ownedCopy("monster")));
    }
}
