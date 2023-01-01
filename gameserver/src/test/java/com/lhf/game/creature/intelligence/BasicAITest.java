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
import com.lhf.Taggable;
import com.lhf.game.battle.Attack;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.conversation.ConversationTreeNode;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;
import com.lhf.server.client.ClientID;
import com.lhf.messages.out.CreatureAffectedMessage;
import com.lhf.messages.out.SpeakingMessage;
import com.lhf.messages.out.OutMessage;

@ExtendWith(MockitoExtension.class)
public class BasicAITest {

    @Spy
    private GroupAIRunner aiRunner = new GroupAIRunner(false, 2, 250, TimeUnit.MILLISECONDS);

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        AIComBundle.setAIRunner(this.aiRunner.start());
    }

    private void sendMsgAndWait(OutMessage message, AIComBundle bundle) {
        bundle.npc.sendMsg(message);
        Assertions.assertDoesNotThrow(
                () -> Mockito.verify(this.aiRunner, timeout(1000).atLeastOnce())
                        .process(bundle.brain.getClientID()));
    }

    @Test
    void testBasicConversation() {
        AIComBundle listener = new AIComBundle();
        ClientMessenger speaker = Mockito.mock(NonPlayerCharacter.class);
        Mockito.when(speaker.getColorTaggedName()).thenReturn("<npc>Joe Speaker</npc>");
        Mockito.when(speaker.getStartTag()).thenReturn("<npc>");
        Mockito.when(speaker.getEndTag()).thenReturn("</npc>");
        ClientID id = new ClientID();
        Mockito.when(speaker.getClientID()).thenReturn(id);

        String body = "I have been addressed";
        ConversationTree tree = new ConversationTree(new ConversationTreeNode(body));

        listener.npc.setConvoTree(tree);

        SpeakingMessage sm = new SpeakingMessage(speaker, "hello", listener.npc);

        sendMsgAndWait(sm, listener);

        Mockito.verify(listener.mockedWrappedHandler, Mockito.timeout(1000)).handleMessage(Mockito.any(),
                Mockito.argThat((command) -> command != null && command.getWhole().contains(body)));
    }

    @Test
    void testAttacked() {
        AIComBundle victim = new AIComBundle();
        AIComBundle attacker = new AIComBundle();
        attacker.npc.setFaction(CreatureFaction.RENEGADE);

        Attack attack = attacker.npc.attack(attacker.npc.getWeapon());
        CreatureEffect effect = attack.getEffects().stream().findFirst().get();
        CreatureAffectedMessage adm = new CreatureAffectedMessage(victim.npc, effect);
        sendMsgAndWait(adm, victim);

        Truth.assertThat(victim.brain.getLastAttacker()).isNull();

        victim.npc.setInBattle(true); // turn it on!

        Truth.assertThat(effect.getDamageResult().getTotal()).isNotEqualTo(0);
        CreatureAffectedMessage doneAttack = new CreatureAffectedMessage(victim.npc, effect);
        sendMsgAndWait(doneAttack, victim);

        Truth.assertThat(victim.brain.getLastAttacker()).isEqualTo(attacker.npc);
        // verify that both attack effects got handled before reaching the final handler
        Mockito.verify(victim.mockedWrappedHandler, Mockito.after(100).never()).handleMessage(Mockito.any(),
                Mockito.any());

    }

    @Test
    void testBadTargetNoTarget() {
        AIComBundle searcher = new AIComBundle();
        searcher.npc.setInBattle(true);
        BadTargetSelectedMessage btsm = new BadTargetSelectedMessage(BadTargetOption.DNE, "bloohoo", new ArrayList<>());
        sendMsgAndWait(btsm, searcher);
        Truth.assertThat(searcher.brain.getLastAttacker()).isNull();
        Mockito.verify(searcher.mockedWrappedHandler, Mockito.timeout(1000)).handleMessage(Mockito.any(),
                Mockito.argThat((command) -> command != null && command.getWhole().contains("pass")));
        Mockito.verifyNoMoreInteractions(searcher.mockedWrappedHandler);
    }

    @Test
    void testBadTargetDiffFaction() {
        AIComBundle searcher = new AIComBundle();
        searcher.npc.setInBattle(true);
        AIComBundle victim = new AIComBundle();
        victim.npc.setFaction(CreatureFaction.MONSTER);
        ArrayList<Taggable> stuff = new ArrayList<>();
        stuff.add(victim.npc);
        BadTargetSelectedMessage btsm = new BadTargetSelectedMessage(BadTargetOption.UNCLEAR, "bloohoo jane", stuff);
        sendMsgAndWait(btsm, searcher);
        Truth.assertThat(searcher.brain.getLastAttacker()).isNotNull();
        Truth.assertThat(searcher.brain.getLastAttacker()).isEqualTo(victim.npc);
        Mockito.verify(searcher.mockedWrappedHandler, Mockito.timeout(1000)).handleMessage(Mockito.any(),
                Mockito.argThat((command) -> command != null && command.getWhole().contains(victim.npc.getName())));

    }

    @Test
    void testBadTargetSameFaction() {
        AIComBundle searcher = new AIComBundle();
        searcher.npc.setInBattle(true);
        AIComBundle samefaction = new AIComBundle();
        ArrayList<Taggable> stuff = new ArrayList<>();
        stuff.add(samefaction.npc);
        BadTargetSelectedMessage btsm = new BadTargetSelectedMessage(BadTargetOption.UNCLEAR, "bloohoo jane", stuff);
        sendMsgAndWait(btsm, searcher);
        Truth.assertThat(searcher.brain.getLastAttacker()).isNull();
        Mockito.verify(searcher.mockedWrappedHandler, Mockito.timeout(1000)).handleMessage(Mockito.any(),
                Mockito.argThat((command) -> command != null && command.getWhole().contains("pass")));

    }

}
