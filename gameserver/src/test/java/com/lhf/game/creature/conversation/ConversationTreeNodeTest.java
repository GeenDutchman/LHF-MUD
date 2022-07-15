package com.lhf.game.creature.conversation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ConversationTreeNodeTest {

    @Test
    void testEmptyNode() {
        ConversationTreeNode node = new ConversationTreeNode();
        assertTrue(node.getBody().contains("nothing"));
    }

    @Test
    void testPopulatedBody() {
        ConversationTreeNode node = new ConversationTreeNode();
        String body = "I have something for you";
        node.addBody(body);
        assertTrue(node.getBody().contains(body));
    }

    @Test
    void testDoublePopulatedBody() {
        ConversationTreeNode node = new ConversationTreeNode();

        String body1 = "I have something for you";
        node.addBody(body1);

        String body2 = "and it should be useful";
        node.addBody(body2);

        assertTrue(node.getBody().contains(body1));
        assertTrue(node.getBody().contains(body2));
    }

}
