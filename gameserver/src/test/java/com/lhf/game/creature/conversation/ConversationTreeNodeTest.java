package com.lhf.game.creature.conversation;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.RichOutput.RichOutputElement;

public class ConversationTreeNodeTest {

    private String basicEmpty = "I have nothing to say to you right now.";

    @Test
    void testEmptyNode() {
        ConversationTreeNode node = new ConversationTreeNode(basicEmpty);
        Truth.assertThat(node.getBodyAsString()).contains("nothing");
    }

    @Test
    void testPopulatedBody() {
        String body = "I have something for you";
        ConversationTreeNode node = new ConversationTreeNode(body);
        Truth.assertThat(node.getBodyAsString()).contains(body);
    }

    @Test
    void testDoublePopulatedBody() {
        String body1 = "I have something for you";
        String body2 = "and it should be useful";
        RichOutputBuilder builder = new RichOutputBuilder().appendString(basicEmpty).appendString(body1)
                .appendString(body2);
        ConversationTreeNode node = new ConversationTreeNode(builder.build());

        Truth.assertThat(node.getBodyAsString()).contains(body1);
        Truth.assertThat(node.getBodyAsString()).contains(body2);
    }

    @Test
    void testGetResult() {
        ConversationTreeNode node = new ConversationTreeNode(basicEmpty);
        node.addPrompt("PROMPT say cheese to anna");

        ConversationTransformer transformer = new ConversationTransformer() {

            @Override
            public RichOutputElement apply(RichOutputElement arg0) {
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
