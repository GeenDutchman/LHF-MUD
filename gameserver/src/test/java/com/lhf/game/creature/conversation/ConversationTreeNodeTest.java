package com.lhf.game.creature.conversation;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.RichOutput.OutputBuilderElement;

public class ConversationTreeNodeTest {

    private String basicEmpty = "I have nothing to say to you right now.";

    @Test
    void testEmptyNode() {
        ConversationTreeNode node = new ConversationTreeNode(basicEmpty);
        Truth.assertThat(node.getBodyAsString()).contains("nothing");
    }

    @Test
    void testPopulatedBody() {
        ConversationTreeNode node = new ConversationTreeNode(basicEmpty);
        String body = "I have something for you";
        node.addBody(body);
        Truth.assertThat(node.getBodyAsString()).contains(body);
    }

    @Test
    void testDoublePopulatedBody() {
        ConversationTreeNode node = new ConversationTreeNode(basicEmpty);

        String body1 = "I have something for you";
        node.addBody(body1);

        String body2 = "and it should be useful";
        node.addBody(body2);

        Truth.assertThat(node.getBodyAsString()).contains(body1);
        Truth.assertThat(node.getBodyAsString()).contains(body2);
    }

    @Test
    void testGetResult() {
        ConversationTreeNode node = new ConversationTreeNode(basicEmpty);
        node.addPrompt("PROMPT say cheese to anna");

        ConversationTransformer transformer = new ConversationTransformer() {

            @Override
            public OutputBuilderElement apply(OutputBuilderElement arg0) {
                return arg0;
            }

            @Override
            public String describePlainOutput() {
                return "Identity";
            }

            @Override
            public String getOutputBody() {
                return "Identity";
            }

        };
        ConversationTreeNodeResult result = ConversationTreeNodeResult.create(transformer, node.getBodySequence(),
                node.getPrompts(), null);
        Truth.assertThat(result.printString()).isEqualTo(node.getBodyAsString());
        Truth.assertThat(result.getPrompts()).containsAtLeastElementsIn(node.getPrompts());
    }

}
