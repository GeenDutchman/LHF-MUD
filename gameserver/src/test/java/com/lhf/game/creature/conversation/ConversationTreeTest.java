package com.lhf.game.creature.conversation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.NonPlayerCharacter;

public class ConversationTreeTest {
    @Test
    void testListenNoStartNode() {
        assertThrows(NullPointerException.class, () -> {
            new ConversationTree(null);
        });
    }

    @Test
    void testIgnoreTalker() {
        ConversationTreeNode node = new ConversationTreeNode();
        ConversationTree tree = new ConversationTree(node);
        Creature talker = new NonPlayerCharacter();
        String response = tree.listen(talker, "unrecongized words like zaosdff");
        assertNull(response);
    }

    @Test
    void testOneTrackConversation() {
        Creature talker = new NonPlayerCharacter();
        ConversationTreeNode start = new ConversationTreeNode();
        ConversationTree tree = new ConversationTree(start);
        String secondBody = "Yes I am!";
        tree.addNode(start.getNodeID(), Pattern.compile("\\bsure\\b.*?", Pattern.CASE_INSENSITIVE),
                new ConversationTreeNode(secondBody));

        String response = tree.listen(talker, "hello there!");
        assertEquals(start.getEmptyStatement(), response);
        response = tree.listen(talker, "Are you sure?");
        assertEquals(secondBody, response);
    }

    @Test
    void testTwoTrackConversation() {
        Creature talker = new NonPlayerCharacter();
        ConversationTreeNode start = new ConversationTreeNode();
        ConversationTree tree = new ConversationTree(start);
        String secondBody = "Yes I am!";
        tree.addNode(start.getNodeID(), Pattern.compile("\\bsure\\b.*?", Pattern.CASE_INSENSITIVE),
                new ConversationTreeNode(secondBody));
        String thirdBody = "Fine!";
        tree.addNode(start.getNodeID(), Pattern.compile("^fine\\b!$", Pattern.CASE_INSENSITIVE),
                new ConversationTreeNode(thirdBody));

        String response = tree.listen(talker, "hello there!");
        assertEquals(start.getEmptyStatement(), response);
        response = tree.listen(talker, "Are you sure?");
        assertEquals(secondBody, response);

        response = tree.listen(talker, "fine!");
        assertNotEquals(thirdBody, response);
        assertEquals(tree.getEndOfConvo(), response);
    }

    @Test
    void testConvoRollover() {
        Creature talker = new NonPlayerCharacter();
        ConversationTreeNode start = new ConversationTreeNode();
        ConversationTree tree = new ConversationTree(start);
        String secondBody = "Yes I am!";
        tree.addNode(start.getNodeID(), Pattern.compile("\\bsure\\b.*?", Pattern.CASE_INSENSITIVE),
                new ConversationTreeNode(secondBody));
        String thirdBody = "Fine!";
        tree.addNode(start.getNodeID(), Pattern.compile("^fine\\b!$", Pattern.CASE_INSENSITIVE),
                new ConversationTreeNode(thirdBody));

        String response = tree.listen(talker, "hello there!");
        assertEquals(start.getEmptyStatement(), response);
        response = tree.listen(talker, "Are you sure?");
        assertEquals(secondBody, response);

        response = tree.listen(talker, "fine!");
        assertNotEquals(thirdBody, response);
        assertEquals(tree.getEndOfConvo(), response);

        response = tree.listen(talker, "hello there!");
        assertEquals(start.getEmptyStatement(), response);

        response = tree.listen(talker, "fine!");
        assertEquals(thirdBody, response);
        assertNotEquals(tree.getEndOfConvo(), response);

        response = tree.listen(talker, "Are you sure?");
        assertNotEquals(secondBody, response);
        assertEquals(tree.getEndOfConvo(), response);
    }
}
