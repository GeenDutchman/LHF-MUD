package com.lhf.game.battle;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.truth.Truth;
import com.lhf.game.creature.Monster;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.game.map.Area;
import com.lhf.messages.MessageMatcher;
import com.lhf.messages.OutMessageType;
import com.lhf.server.client.ClientID;

@ExtendWith(MockitoExtension.class)
public class BattleManagerTest {
        @Test
        void testSimpleBattle() {
                Monster monster = Mockito.mock(Monster.class);
                ClientID monstercClientID = new ClientID();
                NonPlayerCharacter npc = Mockito.mock(NonPlayerCharacter.class);
                ClientID npClientID = new ClientID();
                Area area = Mockito.mock(Area.class);

                Mockito.when(monster.getName()).thenReturn("Monster");
                Mockito.when(monster.getClientID()).thenReturn(monstercClientID);
                Mockito.when(monster.getHealthBucket()).thenReturn(HealthBuckets.HEALTHY);
                // Mockito.when(monster.getColorTaggedName()).thenCallRealMethod();
                Mockito.when(npc.getName()).thenReturn("NPC");
                Mockito.when(npc.getClientID()).thenReturn(npClientID);
                Mockito.when(npc.getHealthBucket()).thenReturn(HealthBuckets.LIGHTLY_INJURED);
                // Mockito.when(npc.getColorTaggedName()).thenCallRealMethod();

                BattleManager battleManager = BattleManager.Builder.getInstance()
                                .addCreature(npc)
                                .addCreature(monster)
                                .Build(area);

                battleManager.startBattle(monster, List.of(npc));
                MessageMatcher startBattle = new MessageMatcher(OutMessageType.START_FIGHT);
                MessageMatcher turnMessage = new MessageMatcher(OutMessageType.BATTLE_ROUND);
                Mockito.verify(npc, Mockito.timeout(1000).atLeastOnce())
                                .sendMsg(Mockito.argThat(startBattle.ownedCopy("npc")));

                Mockito.verify(npc, Mockito.timeout(1000).atLeastOnce())
                                .sendMsg(Mockito.argThat(turnMessage.ownedCopy("npc")));

        }

        @Test
        void testWaitTooLong() throws InterruptedException {
                Monster monster = Mockito.mock(Monster.class);
                ClientID monstercClientID = new ClientID();
                NonPlayerCharacter npc = Mockito.mock(NonPlayerCharacter.class);
                ClientID npClientID = new ClientID();

                Area area = Mockito.mock(Area.class);

                Mockito.when(monster.getName()).thenReturn("Monster");
                // Mockito.when(monster.getColorTaggedName()).thenCallRealMethod();
                // Mockito.when(monster.isAlive()).thenReturn(true);
                Mockito.when(monster.getFaction()).thenReturn(CreatureFaction.RENEGADE);
                Mockito.when(monster.getClientID()).thenReturn(monstercClientID);
                Mockito.when(monster.getHealthBucket()).thenReturn(HealthBuckets.HEALTHY);
                Mockito.when(npc.getName()).thenReturn("NPC");
                // Mockito.when(npc.getColorTaggedName()).thenReturn("NPC");
                // Mockito.when(npc.isAlive()).thenReturn(true);
                Mockito.when(npc.getFaction()).thenReturn(CreatureFaction.RENEGADE);
                Mockito.when(npc.getClientID()).thenReturn(npClientID);
                Mockito.when(npc.getHealthBucket()).thenReturn(HealthBuckets.LIGHTLY_INJURED);

                BattleManager battleManager = BattleManager.Builder.getInstance().addCreature(npc).addCreature(monster)
                                .setWaitMilliseconds(1000).Build(area);
                Truth.assertThat(battleManager.getCreatures()).hasSize(2);

                battleManager.startBattle(monster, List.of(npc));

                MessageMatcher startBattle = new MessageMatcher(OutMessageType.START_FIGHT).setPrint(true);
                MessageMatcher turnMessage = new MessageMatcher(OutMessageType.BATTLE_ROUND,
                                "should enter an action to take for the round")
                                .setPrint(true);
                Mockito.verify(npc, Mockito.timeout(1000).times(1))
                                .sendMsg(Mockito.argThat(startBattle.ownedCopy("npc")));

                Mockito.verify(npc, Mockito.timeout(1000).times(1))
                                .sendMsg(Mockito.argThat(turnMessage.ownedCopy("npc")));

                Mockito.verify(monster, Mockito.timeout(1000).times(1))
                                .sendMsg(Mockito.argThat(turnMessage.ownedCopy("monster")));
        }
}
