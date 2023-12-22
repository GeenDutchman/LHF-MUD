package com.lhf.game.battle;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.truth.Truth;
import com.lhf.game.creature.IMonster;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.game.map.Area;
import com.lhf.messages.MessageMatcher;
import com.lhf.messages.GameEventType;
import com.lhf.messages.out.GameEvent;
import com.lhf.server.client.ClientID;

@ExtendWith(MockitoExtension.class)
public class BattleManagerTest {
        private interface AcceptHook extends Consumer<GameEvent> {
        }

        @Test
        void testSimpleBattle() {
                IMonster monster = Mockito.mock(IMonster.class);
                ClientID monstercClientID = new ClientID();
                AcceptHook monsterHook = Mockito.mock(AcceptHook.class);
                INonPlayerCharacter npc = Mockito.mock(INonPlayerCharacter.class);
                ClientID npClientID = new ClientID();
                AcceptHook npcHook = Mockito.mock(AcceptHook.class);
                Area area = Mockito.mock(Area.class);

                Mockito.when(monster.getName()).thenReturn("Monster");
                Mockito.when(monster.getClientID()).thenReturn(monstercClientID);
                Mockito.when(monster.getHealthBucket()).thenReturn(HealthBuckets.HEALTHY);
                Mockito.when(monster.getAcceptHook()).thenReturn(monsterHook);
                // Mockito.when(monster.getColorTaggedName()).thenCallRealMethod();
                Mockito.when(npc.getName()).thenReturn("NPC");
                Mockito.when(npc.getClientID()).thenReturn(npClientID);
                Mockito.when(npc.getHealthBucket()).thenReturn(HealthBuckets.LIGHTLY_INJURED);
                Mockito.when(npc.getAcceptHook()).thenReturn(npcHook);
                // Mockito.when(npc.getColorTaggedName()).thenCallRealMethod();

                BattleManager battleManager = BattleManager.Builder.getInstance()
                                .addCreature(npc)
                                .addCreature(monster)
                                .Build(area);

                battleManager.startBattle(monster, List.of(npc));
                MessageMatcher startBattle = new MessageMatcher(GameEventType.START_FIGHT);
                MessageMatcher turnMessage = new MessageMatcher(GameEventType.BATTLE_ROUND);
                Mockito.verify(npcHook, Mockito.timeout(1000).atLeastOnce())
                                .accept(Mockito.argThat(startBattle.ownedCopy("npc")));

                Mockito.verify(npcHook, Mockito.timeout(1000).atLeastOnce())
                                .accept(Mockito.argThat(turnMessage.ownedCopy("npc")));

        }

        @Test
        void testWaitTooLong() throws InterruptedException {
                IMonster monster = Mockito.mock(IMonster.class);
                ClientID monstercClientID = new ClientID();
                AcceptHook monsterHook = Mockito.mock(AcceptHook.class);
                INonPlayerCharacter npc = Mockito.mock(INonPlayerCharacter.class);
                ClientID npClientID = new ClientID();
                AcceptHook npcHook = Mockito.mock(AcceptHook.class);

                Area area = Mockito.mock(Area.class);

                Mockito.when(monster.getName()).thenReturn("Monster");
                // Mockito.when(monster.getColorTaggedName()).thenCallRealMethod();
                // Mockito.when(monster.isAlive()).thenReturn(true);
                Mockito.when(monster.getFaction()).thenReturn(CreatureFaction.RENEGADE);
                Mockito.when(monster.getClientID()).thenReturn(monstercClientID);
                Mockito.when(monster.getHealthBucket()).thenReturn(HealthBuckets.HEALTHY);
                Mockito.when(monster.getAcceptHook()).thenReturn(monsterHook);
                Mockito.when(npc.getName()).thenReturn("NPC");
                // Mockito.when(npc.getColorTaggedName()).thenReturn("NPC");
                // Mockito.when(npc.isAlive()).thenReturn(true);
                Mockito.when(npc.getFaction()).thenReturn(CreatureFaction.RENEGADE);
                Mockito.when(npc.getClientID()).thenReturn(npClientID);
                Mockito.when(npc.getHealthBucket()).thenReturn(HealthBuckets.LIGHTLY_INJURED);
                Mockito.when(npc.getAcceptHook()).thenReturn(npcHook);

                BattleManager battleManager = BattleManager.Builder.getInstance().addCreature(npc).addCreature(monster)
                                .setWaitMilliseconds(1000).Build(area);
                Truth.assertThat(battleManager.getCreatures()).hasSize(2);

                battleManager.startBattle(monster, List.of(npc));

                MessageMatcher startBattle = new MessageMatcher(GameEventType.START_FIGHT).setPrint(true);
                MessageMatcher turnMessage = new MessageMatcher(GameEventType.BATTLE_ROUND,
                                "should enter an action to take for the round")
                                .setPrint(true);
                Mockito.verify(npcHook, Mockito.timeout(1000).times(1))
                                .accept(Mockito.argThat(startBattle.ownedCopy("npc")));

                Mockito.verify(npcHook, Mockito.timeout(1000).times(1))
                                .accept(Mockito.argThat(turnMessage.ownedCopy("npc")));

                Mockito.verify(monsterHook, Mockito.timeout(1000).times(1))
                                .accept(Mockito.argThat(turnMessage.ownedCopy("monster")));
        }
}
