package com.lhf.game.creature.conversation;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

public class ConversationTreeNodeTest {

    private String basicEmpty = "I have nothing to say to you right now.";

    @Test
    void testEmptyNode() {
        ConversationTreeNode node = new ConversationTreeNode(basicEmpty);
        Truth.assertThat(node.getBody()).contains("nothing");
    }

    @Test
    void testPopulatedBody() {
        ConversationTreeNode node = new ConversationTreeNode(basicEmpty);
        String body = "I have something for you";
        node.addBody(body);
        Truth.assertThat(node.getBody()).contains(body);
    }

    @Test
    void testDoublePopulatedBody() {
        ConversationTreeNode node = new ConversationTreeNode(basicEmpty);

        String body1 = "I have something for you";
        node.addBody(body1);

        String body2 = "and it should be useful";
        node.addBody(body2);

        Truth.assertThat(node.getBody()).contains(body1);
        Truth.assertThat(node.getBody()).contains(body2);
    }

    @Test
    void testGetResult() {
        ConversationTreeNode node = new ConversationTreeNode(basicEmpty);
        node.addPrompt("PROMPT say cheese to anna");

        ConversationTreeNodeResult result = node.getResult();
        Truth.assertThat(result.getBody()).isEqualTo(node.getBody());
        Truth.assertThat(result.getPrompts()).containsAtLeastElementsIn(node.getPrompts());
    }

}
