package com.lhf.game.creature.intelligence;

import static org.mockito.Mockito.timeout;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.truth.Truth;
import com.google.common.truth.Truth8;
import com.lhf.Taggable;
import com.lhf.game.battle.Attack;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.conversation.ConversationTreeNode;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.map.SubArea.SubAreaSort;
import com.lhf.messages.events.BadTargetSelectedEvent;
import com.lhf.messages.events.BadTargetSelectedEvent.BadTargetOption;
import com.lhf.messages.events.CreatureAffectedEvent;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.SpeakingEvent;
import com.lhf.server.client.Client.ClientID;

@ExtendWith(MockitoExtension.class)
public class BasicAITest {

        @Spy
        private GroupAIRunner aiRunner = new GroupAIRunner(false, 2, 250, TimeUnit.MILLISECONDS);

        @BeforeEach
        public void setUp() throws Exception {
                MockitoAnnotations.openMocks(this);
                AIComBundle.setAIRunner(this.aiRunner.start());
        }

        private void sendMsgAndWait(GameEvent message, AIComBundle bundle) {
                INonPlayerCharacter.eventAccepter.accept(bundle.getNPC(), message);
                Assertions.assertDoesNotThrow(
                                () -> Mockito.verify(this.aiRunner, timeout(1000).atLeastOnce())
                                                .process(bundle.brain.getClientID()));
        }

        @Test
        void testBasicConversation() {
                AIComBundle listener = new AIComBundle();
                INonPlayerCharacter speaker = Mockito.mock(INonPlayerCharacter.class);
                Mockito.when(speaker.getColorTaggedName()).thenReturn("<npc>Joe Speaker</npc>");
                Mockito.when(speaker.getStartTag()).thenReturn("<npc>");
                Mockito.when(speaker.getEndTag()).thenReturn("</npc>");
                ClientID clientID = new ClientID();
                Mockito.when(speaker.getClientID()).thenReturn(clientID);

                String body = "I have been addressed";
                ConversationTree tree = new ConversationTree(new ConversationTreeNode(body));

                listener.getNPC().setConvoTree(tree);

                SpeakingEvent sm = SpeakingEvent.getBuilder().setSayer(speaker).setMessage("hello")
                                .setHearer(listener.getNPC())
                                .Build();

                sendMsgAndWait(sm, listener);

                Mockito.verify(listener.mockedWrappedHandler, Mockito.timeout(1000)).handle(Mockito.any(),
                                Mockito.argThat((command) -> command != null && command.getWhole().contains(body)));
        }

        @Test
        void testAttacked() {
                AIComBundle victim = new AIComBundle();
                AIComBundle attacker = new AIComBundle();
                attacker.getNPC().setFaction(CreatureFaction.RENEGADE);

                Attack attack = attacker.getNPC().attack(attacker.getNPC().defaultWeapon());
                CreatureEffect effect = attack.getEffects().stream().findFirst().get();
                CreatureAffectedEvent adm = CreatureAffectedEvent.getBuilder().setAffected(victim.getNPC())
                                .fromCreatureEffect(effect)
                                .Build();
                sendMsgAndWait(adm, victim);

                Truth8.assertThat(victim.getNPC().getHarmMemories().getLastAttackerName()).isEmpty();

                victim.getNPC().addSubArea(SubAreaSort.BATTLE); // turn it on!

                Truth.assertThat(effect.getApplicationDamageResult(null).getTotal()).isNotEqualTo(0);
                CreatureAffectedEvent doneAttack = CreatureAffectedEvent.getBuilder().setAffected(victim.getNPC())
                                .fromCreatureEffect(effect).Build();
                sendMsgAndWait(doneAttack, victim);

                Mockito.verify(victim.sssb, Mockito.timeout(1000)).send(doneAttack);

                Truth.assertWithMessage("The victim should remember an attacker")
                                .that(victim.getNPC().getHarmMemories().getLastAttackerName().isPresent()).isTrue();
                Truth.assertWithMessage("The victim should remember the attacker's name")
                                .that(victim.getNPC().getHarmMemories().getLastAttackerName().get())
                                .isEqualTo(attacker.getNPC().getName());
                // verify that both attack effects got handled before reaching the final handler
                Mockito.verify(victim.mockedWrappedHandler, Mockito.after(100).never()).handle(Mockito.any(),
                                Mockito.any());

        }

        @Test
        void testBadTargetDiffFaction() {
                AIComBundle searcher = new AIComBundle();
                searcher.getNPC().addSubArea(SubAreaSort.BATTLE);
                AIComBundle victim = new AIComBundle();
                victim.getNPC().setFaction(CreatureFaction.MONSTER);
                ArrayList<Taggable> stuff = new ArrayList<>();
                stuff.add(victim.getNPC());
                BadTargetSelectedEvent btsm = BadTargetSelectedEvent.getBuilder().setBde(BadTargetOption.UNCLEAR)
                                .setBadTarget("bloohoo jane").setPossibleTargets(stuff).Build();
                sendMsgAndWait(btsm, searcher);
                Truth.assertThat(searcher.getNPC().getHarmMemories().getLastAttackerName().isEmpty()).isTrue();
        }

        @Test
        void testBadTargetSameFaction() {
                AIComBundle searcher = new AIComBundle();
                searcher.getNPC().addSubArea(SubAreaSort.BATTLE);
                AIComBundle samefaction = new AIComBundle();
                ArrayList<Taggable> stuff = new ArrayList<>();
                stuff.add(samefaction.getNPC());
                BadTargetSelectedEvent btsm = BadTargetSelectedEvent.getBuilder().setBde(BadTargetOption.UNCLEAR)
                                .setBadTarget("bloohoo jane").setPossibleTargets(stuff).Build();
                sendMsgAndWait(btsm, searcher);
                Truth8.assertThat(searcher.getNPC().getHarmMemories().getLastAttackerName()).isEmpty();
        }

}
