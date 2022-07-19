package com.lhf.game.creature.conversation;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

public class ConversationTreeNodeTest {

    @Test
    void testEmptyNode() {
        ConversationTreeNode node = new ConversationTreeNode();
        Truth.assertThat(node.getBody()).contains("nothing");
    }

    @Test
    void testPopulatedBody() {
        ConversationTreeNode node = new ConversationTreeNode();
        String body = "I have something for you";
        node.addBody(body);
        Truth.assertThat(node.getBody()).contains(body);
    }

    @Test
    void testDoublePopulatedBody() {
        ConversationTreeNode node = new ConversationTreeNode();

        String body1 = "I have something for you";
        node.addBody(body1);

        String body2 = "and it should be useful";
        node.addBody(body2);

        Truth.assertThat(node.getBody()).contains(body1);
        Truth.assertThat(node.getBody()).contains(body2);
    }

    @Test
    void testGetResult() {
        ConversationTreeNode node = new ConversationTreeNode();
        node.addPrompt("PROMPT GUID-HERE say cheese to anna");

        ConversationTreeNodeResult result = node.getResult();
        Truth.assertThat(result.getBody()).isEqualTo(node.getBody());
        Truth.assertThat(result.getPrompts()).containsAtLeastElementsIn(node.getPrompts());
    }

}
