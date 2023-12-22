package com.lhf.game.creature.intelligence.handlers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
                SpeakingMessage sm = SpeakingMessage.getBuilder().setSayer(speaker.npc).setMessage("hello")
                                .setHearer(listener.npc).Build();
                AIComBundle.eventAccepter.accept(listener.npc, sm);

                Mockito.verify(listener.sssb, Mockito.timeout(1000)).send(sm);
                Mockito.verify(listener.mockedWrappedHandler, Mockito.timeout(1000)).handle(Mockito.any(),
                                Mockito.argThat((command) -> command != null && command.getWhole().contains(body)));
                Mockito.verify(listener.mockedWrappedHandler, Mockito.timeout(1000)).handle(Mockito.any(),
                                Mockito.argThat((command) -> command != null
                                                && command.getWhole().contains(sayMessage)));

        }

        @Test
        void testOtherPrompter() {
                AIComBundle speaker = new AIComBundle();
                AIComBundle listener = new AIComBundle();
                SpokenPromptChunk chunk = new SpokenPromptChunk();
                chunk.addPrompter(speaker.npc.getClientID());
                listener.brain.addHandler(OutMessageType.SPEAKING, chunk);

                String prompt = "NONOBJECT";
                SpeakingMessage sm = SpeakingMessage.getBuilder().setSayer(speaker.npc)
                                .setMessage("PROMPT SEE " + prompt)
                                .setHearer(listener.npc).Build();
                AIComBundle.eventAccepter.accept(listener.npc, sm);

                Mockito.verify(listener.mockedWrappedHandler, Mockito.timeout(1000)).handle(Mockito.any(),
                                Mockito.argThat((command) -> command != null && command.getWhole().contains(prompt)));

        }

        @Test
        void testNotPrompter() {
                AIComBundle speaker = new AIComBundle();
                AIComBundle listener = new AIComBundle();
                SpokenPromptChunk chunk = new SpokenPromptChunk();
                listener.brain.addHandler(OutMessageType.SPEAKING, chunk);

                String prompt = "NONOBJECT";
                SpeakingMessage sm = SpeakingMessage.getBuilder().setSayer(speaker.npc)
                                .setMessage("PROMPT SEE " + prompt)
                                .setHearer(listener.npc).Build();
                AIComBundle.eventAccepter.accept(listener.npc, sm);

                Mockito.verifyNoInteractions(listener.mockedWrappedHandler);

        }
}
