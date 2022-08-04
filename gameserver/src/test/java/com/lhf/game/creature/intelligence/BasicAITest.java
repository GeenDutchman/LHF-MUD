package com.lhf.game.creature.intelligence;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.Taggable;
import com.lhf.game.EntityEffector.EffectPersistence;
import com.lhf.game.creature.CreatureEffector;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.conversation.ConversationTreeNode;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;
import com.lhf.messages.out.CreatureAffectedMessage;
import com.lhf.messages.out.SpeakingMessage;

public class BasicAITest {

    @Test
    void testBasicConversation() {
        AIComBundle listener = new AIComBundle();
        AIComBundle speaker = new AIComBundle();
        String body = "I have been addressed";
        ConversationTree tree = new ConversationTree(new ConversationTreeNode(body));

        listener.npc.setConvoTree(tree);

        SpeakingMessage sm = new SpeakingMessage(speaker.npc, "hello", listener.npc);

        listener.npc.sendMsg(sm);

        Truth.assertThat(listener.sent.size()).isAtLeast(1);
        Truth.assertThat(listener.sent.get(0).toString()).contains(body);
    }

    @Test
    void testAttacked() {
        AIComBundle victim = new AIComBundle();
        AIComBundle attacker = new AIComBundle();

        CreatureEffector effects = new CreatureEffector(attacker.npc, EffectPersistence.INSTANT);
        CreatureAffectedMessage adm = new CreatureAffectedMessage(victim.npc, effects);
        victim.npc.sendMsg(adm);

        Truth.assertThat(victim.sent).isEmpty();
        Truth.assertThat(victim.brain.getLastAttacker()).isNull();

        victim.npc.setInBattle(true); // turn it on!

        effects = new CreatureEffector(attacker.npc, EffectPersistence.INSTANT);
        CreatureAffectedMessage notdone = new CreatureAffectedMessage(victim.npc, effects);
        victim.npc.sendMsg(notdone);
        Truth.assertThat(victim.sent).isEmpty();
        Truth.assertThat(victim.brain.getLastAttacker()).isNull();

        victim.npc.sendMsg(adm);
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
