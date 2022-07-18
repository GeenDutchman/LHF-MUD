package com.lhf.game.creature.conversation;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
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
        Truth.assertThat(response).isNull();
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
        Truth.assertThat(response).isEqualTo(start.getEmptyStatement());
        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response).isEqualTo(secondBody);
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
        Truth.assertThat(response).isEqualTo(start.getEmptyStatement());
        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response).isEqualTo(secondBody);

        response = tree.listen(talker, "fine!");
        Truth.assertThat(response).isNotEqualTo(thirdBody);
        Truth.assertThat(response).isEqualTo(tree.getEndOfConvo());
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
        Truth.assertThat(response).isEqualTo(start.getEmptyStatement());
        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response).isEqualTo(secondBody);

        response = tree.listen(talker, "fine!");
        Truth.assertThat(response).isNotEqualTo(thirdBody);
        Truth.assertThat(response).isEqualTo(tree.getEndOfConvo());

        response = tree.listen(talker, "hello there!");
        Truth.assertThat(response).isEqualTo(start.getEmptyStatement());

        response = tree.listen(talker, "fine!");
        Truth.assertThat(response).isEqualTo(thirdBody);
        Truth.assertThat(response).isNotEqualTo(tree.getEndOfConvo());

        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response).isNotEqualTo(secondBody);
        Truth.assertThat(response).isEqualTo(tree.getEndOfConvo());
    }

    @Test
    void testRepeatNode() {
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
        Truth.assertThat(response).isEqualTo(start.getEmptyStatement());
        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response).isEqualTo(secondBody);

        response = tree.listen(talker, "what was that again?");
        Truth.assertThat(response).isEqualTo(secondBody);
    }

    @Test
    void testHightlightNext() {
        Creature talker = new NonPlayerCharacter();
        String body1 = "Hello there new young traveller!";
        ConversationTreeNode start = new ConversationTreeNode(body1);
        ConversationTree tree = new ConversationTree(start);
        String body2 = "Why yes, you are a traveller, are you not?";
        ConversationTreeNode second = new ConversationTreeNode(body2);

        tree.addNode(start.getNodeID(), Pattern.compile("\\btraveller\\b", Pattern.CASE_INSENSITIVE), second);
        String response = tree.listen(talker, "hello there!");
        Truth.assertThat(response).contains("<convo>traveller</convo>");
    }
}
