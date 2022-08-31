package com.lhf.game.creature.intelligence.handlers;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.conversation.ConversationTreeNode;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.out.SpeakingMessage;

public class SpokenPromptChunkTest {
    @Test
    void testPromptSelf() {
        SpokenPromptChunk chunk = new SpokenPromptChunk();
        AIComBundle listener = new AIComBundle();
        listener.brain.addHandler(OutMessageType.SPEAKING, chunk);

        String body = "I have been addressed";
        ConversationTreeNode node = new ConversationTreeNode(body);
        String sayMessage = "wakarimasen";
        node.addPrompt("SAY " + sayMessage);
        ConversationTree tree = new ConversationTree(node);
        listener.npc.setConvoTree(tree);

        AIComBundle speaker = new AIComBundle();
        SpeakingMessage sm = new SpeakingMessage(speaker.npc, "hello", listener.npc);
        listener.npc.sendMsg(sm);

        Truth.assertThat(listener.sent.size()).isAtLeast(2);
        Truth.assertThat(listener.sent.get(0).toString()).contains(body);
        Truth.assertThat(listener.sent.get(1).toString()).contains(sayMessage);

    }

    @Test
    void testOtherPrompter() {
        AIComBundle speaker = new AIComBundle();
        AIComBundle listener = new AIComBundle();
        SpokenPromptChunk chunk = new SpokenPromptChunk();
        chunk.addPrompter(speaker.brain.getClientID());
        listener.brain.addHandler(OutMessageType.SPEAKING, chunk);

        String prompt = "NONOBJECT";
        SpeakingMessage sm = new SpeakingMessage(speaker.npc, "PROMPT SEE " + prompt, listener.npc);
        listener.npc.sendMsg(sm);

        Truth.assertThat(listener.sent.size()).isAtLeast(1);
        Truth.assertThat(listener.sent.get(0).toString()).contains(prompt);

    }

    @Test
    void testNotPrompter() {
        AIComBundle speaker = new AIComBundle();
        AIComBundle listener = new AIComBundle();
        SpokenPromptChunk chunk = new SpokenPromptChunk();
        listener.brain.addHandler(OutMessageType.SPEAKING, chunk);

        String prompt = "NONOBJECT";
        SpeakingMessage sm = new SpeakingMessage(speaker.npc, "PROMPT SEE " + prompt, listener.npc);
        listener.npc.sendMsg(sm);

        Truth.assertThat(listener.sent.size()).isEqualTo(0);

    }
}
