package com.lhf.game.creature.intelligence.handlers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.conversation.ConversationTreeNode;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.SpeakingEvent;

public class SpokenPromptChunkTest {
        @Test
        void testPromptSelf() {
                SpokenPromptChunk chunk = new SpokenPromptChunk();
                AIComBundle listener = new AIComBundle();
                listener.brain.addHandler(GameEventType.SPEAKING, chunk);

                String body = "I have been addressed";
                ConversationTreeNode node = new ConversationTreeNode(body);
                String sayMessage = "wakarimasen";
                node.addPrompt("SAY " + sayMessage);
                ConversationTree tree = new ConversationTree(node);
                listener.getNPC().setConvoTree(tree);

                AIComBundle speaker = new AIComBundle();
                SpeakingEvent sm = SpeakingEvent.getBuilder().setSayer(speaker.getNPC()).setMessage("hello")
                                .setHearer(listener.getNPC()).Build();
                AIComBundle.eventAccepter.accept(listener.getNPC(), sm);

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
                chunk.addPrompter(speaker.getNPC().getEventProcessorID());
                listener.brain.addHandler(GameEventType.SPEAKING, chunk);

                String prompt = "NONOBJECT";
                SpeakingEvent sm = SpeakingEvent.getBuilder().setSayer(speaker.getNPC())
                                .setMessage("PROMPT SEE " + prompt)
                                .setHearer(listener.getNPC()).Build();
                AIComBundle.eventAccepter.accept(listener.getNPC(), sm);

                Mockito.verify(listener.mockedWrappedHandler, Mockito.timeout(1000)).handle(Mockito.any(),
                                Mockito.argThat((command) -> command != null && command.getWhole().contains(prompt)));

        }

        @Test
        void testNotPrompter() {
                AIComBundle speaker = new AIComBundle();
                AIComBundle listener = new AIComBundle();
                SpokenPromptChunk chunk = new SpokenPromptChunk();
                listener.brain.addHandler(GameEventType.SPEAKING, chunk);

                String prompt = "NONOBJECT";
                SpeakingEvent sm = SpeakingEvent.getBuilder().setSayer(speaker.getNPC())
                                .setMessage("PROMPT SEE " + prompt)
                                .setHearer(listener.getNPC()).Build();
                AIComBundle.eventAccepter.accept(listener.getNPC(), sm);

                Mockito.verifyNoInteractions(listener.mockedWrappedHandler);

        }
}
