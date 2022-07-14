package com.lhf.game.creature.conversation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class ConversationTreeNodeTest {

    @Test
    void testEmptyNode() {
        ConversationTreeNode node = new ConversationTreeNode();
        assertTrue(node.getBody().contains("nothing"));
        assertEquals(0, node.getForwardMap().size());
        assertNull(node.getNextNodeID("Hello There"));
    }

    @Test
    void testSingleForwardRef() {
        ConversationTreeNode node = new ConversationTreeNode();
        UUID test2 = UUID.randomUUID();
        String body = "I have something for you";
        String keyword = "something";
        node.addBodyWithForwardRef(body, keyword, test2);
        assertTrue(node.getBody().contains(body));
        assertEquals(1, node.getForwardMap().size());
        assertEquals(test2, node.getForwardMap().get(keyword));
        assertNull(node.getNextNodeID("Hello There"));
        assertEquals(test2, node.getNextNodeID(keyword));
    }

    @Test
    void testBranchingNode() {
        ConversationTreeNode node = new ConversationTreeNode();

        UUID test1 = UUID.randomUUID();
        String body1 = "I have something for you";
        String keyword1 = "something";
        node.addBodyWithForwardRef(body1, keyword1, test1);

        UUID test2 = UUID.randomUUID();
        String body2 = "and it should be useful";
        String keyword2 = "useful";
        node.addBodyWithForwardRef(body2, keyword2, test2);

        assertTrue(node.getBody().contains(body1));
        assertTrue(node.getBody().contains(body2));
        assertEquals(2, node.getForwardMap().size());
        assertEquals(test1, node.getForwardMap().get(keyword1));
        assertEquals(test2, node.getForwardMap().get(keyword2));
        assertNull(node.getNextNodeID("Hello There"));
        assertEquals(test1, node.getNextNodeID(keyword1));
        assertEquals(test2, node.getNextNodeID(keyword2));
    }

}
