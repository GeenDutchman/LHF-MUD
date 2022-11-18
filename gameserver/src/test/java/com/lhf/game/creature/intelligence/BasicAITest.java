package com.lhf.game.creature.intelligence;

import static org.mockito.Mockito.timeout;

import java.util.ArrayList;

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
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.conversation.ConversationTreeNode;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;
import com.lhf.messages.out.CreatureAffectedMessage;
import com.lhf.messages.out.SpeakingMessage;

@ExtendWith(MockitoExtension.class)
public class BasicAITest {

    @Spy
    private GroupAIRunner aiRunner = new GroupAIRunner(true);

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        AIComBundle.setAIRunner(this.aiRunner);
    }

    @Test
    void testBasicConversation() {
        AIComBundle listener = new AIComBundle();
        AIComBundle speaker = new AIComBundle();
        String body = "I have been addressed";
        ConversationTree tree = new ConversationTree(new ConversationTreeNode(body));

        listener.npc.setConvoTree(tree);

        SpeakingMessage sm = new SpeakingMessage(speaker.npc, "hello", listener.npc);

        listener.npc.sendMsg(sm);

        Assertions.assertDoesNotThrow(
                () -> Mockito.verify(this.aiRunner, timeout(7000).atLeastOnce()).process(listener.brain.getClientID()));

        Truth.assertThat(listener.sent.size()).isAtLeast(1);
        Truth.assertThat(listener.sent.get(0).toString()).contains(body);
    }

    @Test
    void testAttacked() {
        AIComBundle victim = new AIComBundle();
        AIComBundle attacker = new AIComBundle();

        Attack attack = attacker.npc.attack(attacker.npc.getWeapon());
        CreatureEffect effect = attack.getEffects().stream().findFirst().get();
        CreatureAffectedMessage adm = new CreatureAffectedMessage(victim.npc, effect);
        victim.npc.sendMsg(adm);

        Truth.assertThat(victim.sent).isEmpty();
        Truth.assertThat(victim.brain.getLastAttacker()).isNull();

        victim.npc.setInBattle(true); // turn it on!

        Truth.assertThat(effect.getDamageResult().getTotal()).isNotEqualTo(0);
        CreatureAffectedMessage doneAttack = new CreatureAffectedMessage(victim.npc, effect);
        victim.npc.sendMsg(doneAttack);
        Truth.assertThat(victim.sent).isEmpty();
        Truth.assertThat(victim.brain.getLastAttacker()).isEqualTo(attacker.npc);

    }

    @Test
    void testBadTarget() {
        AIComBundle searcher = new AIComBundle();
        searcher.npc.setInBattle(true);
        BadTargetSelectedMessage btsm = new BadTargetSelectedMessage(BadTargetOption.DNE, "bloohoo", new ArrayList<>());
        searcher.npc.sendMsg(btsm);
        Truth.assertThat(searcher.brain.getLastAttacker()).isNull();
        Truth.assertThat(searcher.sent).isNotEmpty();
        Truth.assertThat(searcher.sent).hasSize(1);
        Truth.assertThat(searcher.sent.get(0).toString()).ignoringCase().contains("pass");

        AIComBundle victim = new AIComBundle();
        victim.npc.setFaction(CreatureFaction.MONSTER);
        ArrayList<Taggable> stuff = new ArrayList<>();
        stuff.add(victim.npc);
        btsm = new BadTargetSelectedMessage(BadTargetOption.UNCLEAR, "bloohoo jane", stuff);
        searcher.npc.sendMsg(btsm);
        Truth.assertThat(searcher.brain.getLastAttacker()).isNotNull();
        Truth.assertThat(searcher.brain.getLastAttacker()).isEqualTo(victim.npc);
        Truth.assertThat(searcher.sent).isNotEmpty();
        Truth.assertThat(searcher.sent).hasSize(2);
        Truth.assertThat(searcher.sent.get(1).toString()).ignoringCase().contains(victim.npc.getName());

        AIComBundle samefaction = new AIComBundle();
        stuff = new ArrayList<>();
        stuff.add(samefaction.npc);
        btsm = new BadTargetSelectedMessage(BadTargetOption.UNCLEAR, "bloohoo jane", stuff);
        searcher.npc.sendMsg(btsm);
        Truth.assertThat(searcher.brain.getLastAttacker()).isNull();
        Truth.assertThat(searcher.sent).isNotEmpty();
        Truth.assertThat(searcher.sent).hasSize(3);
        Truth.assertThat(searcher.sent.get(2).toString()).ignoringCase().contains("pass");
    }

}
