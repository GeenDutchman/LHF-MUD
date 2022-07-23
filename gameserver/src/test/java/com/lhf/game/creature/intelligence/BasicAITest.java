package com.lhf.game.creature.intelligence;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.google.common.truth.Truth;
import com.lhf.Taggable;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.conversation.ConversationTreeNode;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.out.AttackDamageMessage;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.SpeakingMessage;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;
import com.lhf.server.client.StringBufferSendStrategy;

public class BasicAITest {
    private class ComBundle implements MessageHandler {
        public NonPlayerCharacter npc;
        public BasicAI brain;
        public StringBufferSendStrategy sssb;
        public ArrayList<Command> sent;

        public ComBundle() {
            this.npc = new NonPlayerCharacter();
            this.brain = new BasicAI(this.npc);
            this.sssb = new StringBufferSendStrategy();
            brain.SetOut(this.sssb);
            this.npc.setController(this.brain);
            this.sent = new ArrayList<>();
            this.npc.setSuccessor(this);
        }

        public String read() {
            String buffer = this.sssb.read();
            System.out.println("***********************" + this.npc.getName() + "**********************");
            System.out.println(buffer);
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            return buffer;
        }

        public void clear() {
            this.sssb.clear();
        }

        @Override
        public void setSuccessor(MessageHandler successor) {
            // no -op
        }

        @Override
        public MessageHandler getSuccessor() {
            return null;
        }

        @Override
        public Map<CommandMessage, String> getCommands() {
            return null;
        }

        @Override
        public Boolean handleMessage(CommandContext ctx, Command msg) {
            System.out.println(msg.toString());
            this.sent.add(msg);
            return true;
        }

    }

    @Test
    void testBasicConversation() {
        ComBundle listener = new ComBundle();
        ComBundle speaker = new ComBundle();
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
        ComBundle victim = new ComBundle();
        ComBundle attacker = new ComBundle();

        AttackDamageMessage adm = new AttackDamageMessage(attacker.npc, victim.npc);
        victim.npc.sendMsg(adm);

        Truth.assertThat(victim.sent).isEmpty();
        Truth.assertThat(victim.brain.getLastAttacker()).isNull();

        victim.npc.setInBattle(true); // turn it on!

        AttackDamageMessage notdone = new AttackDamageMessage(attacker.npc, attacker.npc);
        victim.npc.sendMsg(notdone);
        Truth.assertThat(victim.sent).isEmpty();
        Truth.assertThat(victim.brain.getLastAttacker()).isNull();

        victim.npc.sendMsg(adm);
        Truth.assertThat(victim.sent).isEmpty();
        Truth.assertThat(victim.brain.getLastAttacker()).isEqualTo(attacker.npc);
    }

    @Test
    void testBadTarget() {
        ComBundle searcher = new ComBundle();
        searcher.npc.setInBattle(true);
        BadTargetSelectedMessage btsm = new BadTargetSelectedMessage(BadTargetOption.DNE, "bloohoo", new ArrayList<>());
        searcher.npc.sendMsg(btsm);
        Truth.assertThat(searcher.brain.getLastAttacker()).isNull();
        Truth.assertThat(searcher.sent).isNotEmpty();
        Truth.assertThat(searcher.sent).hasSize(1);
        Truth.assertThat(searcher.sent.get(0).toString()).ignoringCase().contains("turnwaster");

        ComBundle victim = new ComBundle();
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

        ComBundle samefaction = new ComBundle();
        stuff = new ArrayList<>();
        stuff.add(samefaction.npc);
        btsm = new BadTargetSelectedMessage(BadTargetOption.UNCLEAR, "bloohoo jane", stuff);
        searcher.npc.sendMsg(btsm);
        Truth.assertThat(searcher.brain.getLastAttacker()).isNull();
        Truth.assertThat(searcher.sent).isNotEmpty();
        Truth.assertThat(searcher.sent).hasSize(3);
        Truth.assertThat(searcher.sent.get(2).toString()).ignoringCase().contains("Turnwaster");
    }

}
