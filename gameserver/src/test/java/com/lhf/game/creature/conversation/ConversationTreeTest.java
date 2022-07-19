package com.lhf.game.creature.conversation;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.creature.conversation.ConversationContext.ConversationContextKey;

public class ConversationTreeTest {
    @Test
    void testListenNoStartNode() {
        assertThrows(NullPointerException.class, () -> {
            new ConversationTree(null);
        });
    }

    @Test
    void testIgnoreUngreeted() {
        ConversationTreeNode node = new ConversationTreeNode();
        ConversationTree tree = new ConversationTree(node);
        Creature talker = new NonPlayerCharacter();
        ConversationTreeNodeResult response = tree.listen(talker, "unrecongized words like zaosdff");
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

        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.getBody()).isEqualTo(start.getEmptyStatement());
        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response.getBody()).isEqualTo(secondBody);
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

        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.getBody()).isEqualTo(start.getEmptyStatement());
        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response.getBody()).isEqualTo(secondBody);

        response = tree.listen(talker, "fine!");
        Truth.assertThat(response.getBody()).isNotEqualTo(thirdBody);
        Truth.assertThat(response.getBody()).isEqualTo(tree.getEndOfConvo());
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

        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.getBody()).isEqualTo(start.getEmptyStatement());
        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response.getBody()).isEqualTo(secondBody);

        response = tree.listen(talker, "fine!");
        Truth.assertThat(response.getBody()).isNotEqualTo(thirdBody);
        Truth.assertThat(response.getBody()).isEqualTo(tree.getEndOfConvo());

        response = tree.listen(talker, "fine!");
        Truth.assertThat(response.getBody()).isEqualTo(thirdBody);
        Truth.assertThat(response.getBody()).isNotEqualTo(tree.getEndOfConvo());

        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response.getBody()).isNotEqualTo(secondBody);
        Truth.assertThat(response.getBody()).isEqualTo(tree.getEndOfConvo());
    }

    @Test
    void testRememberSpot() {
        Creature talker = new NonPlayerCharacter();
        ConversationTreeNode start = new ConversationTreeNode();
        ConversationTree tree = new ConversationTree(start);
        String secondBody = "Yes I am!";
        ConversationTreeNode secondNode = new ConversationTreeNode(secondBody);
        tree.addNode(start.getNodeID(), Pattern.compile("\\bsure\\b.*?", Pattern.CASE_INSENSITIVE),
                secondNode);
        String thirdBody = "Fine!";
        tree.addNode(secondNode.getNodeID(), Pattern.compile("^fine\\b!$", Pattern.CASE_INSENSITIVE),
                new ConversationTreeNode(thirdBody));

        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.getBody()).isEqualTo(start.getEmptyStatement());
        response = tree.listen(talker, "zippity doo dah");
        Truth.assertThat(response.getBody()).doesNotContain(tree.getEndOfConvo());
        Truth.assertThat(response.getBody()).isEqualTo(tree.getNotRecognized());
        response = tree.listen(talker, "what was that again?");
        Truth.assertThat(response.getBody()).isEqualTo(start.getEmptyStatement());
        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response.getBody()).isEqualTo(secondNode.getBody());
        response = tree.listen(talker, "zippity eh");
        Truth.assertThat(response.getBody()).isEqualTo(tree.getNotRecognized());
        response = tree.listen(talker, "what was that again?");
        Truth.assertThat(response.getBody()).isEqualTo(secondNode.getBody());

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

        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.getBody()).isEqualTo(start.getEmptyStatement());
        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response.getBody()).isEqualTo(secondBody);

        response = tree.listen(talker, "what was that again?");
        Truth.assertThat(response.getBody()).isEqualTo(secondBody);
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
        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.getBody()).contains("<convo>traveller</convo>");
    }

    @Test
    void testGreetBack() {
        Creature talker = new NonPlayerCharacter();
        ConversationTreeNode start = new ConversationTreeNode(
                "I greet you back " + ConversationContextKey.TALKER_TAGGED_NAME);
        ConversationTree tree = new ConversationTree(start);

        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.getBody()).contains(talker.getName());
    }

    @Test
    void testForbidBranch() {
        Creature welcome = new NonPlayerCharacter();
        Creature unwelcome = new NonPlayerCharacter();
        while (welcome.getName().equals(unwelcome.getName())) {
            unwelcome.setName(unwelcome.getName() + "x");
        }
        Truth.assertThat(unwelcome.getName()).isNotEqualTo(welcome.getName());

        ConversationTreeNode start = new ConversationTreeNode(
                "I greet you back " + ConversationContextKey.TALKER_TAGGED_NAME);
        start.addBody("I will test the welcome and the unwelcome both");
        ConversationTree tree = new ConversationTree(start);
        ConversationTreeNode oneWay = new ConversationTreeNode("I am friendly");
        ConversationTreeNode otherWay = new ConversationTreeNode("I am not friendly");

        ConversationTreeBranch oneBranch = tree.addNode(start.getNodeID(),
                Pattern.compile("\\bwelcome\\b", Pattern.CASE_INSENSITIVE), oneWay);
        tree.addNode(start.getNodeID(),
                Pattern.compile("\\bunwelcome\\b", Pattern.CASE_INSENSITIVE), otherWay);

        // unwelcome is not welcome to the oneWay
        oneBranch.addRule(ConversationContextKey.TALKER_NAME, Pattern.compile("\\b" + unwelcome.getName() + "\\b"));

        // welcome
        ConversationTreeNodeResult response = tree.listen(welcome, "hello there!");
        Truth.assertThat(response.getBody()).ignoringCase().contains("<convo>welcome</convo>");
        Truth.assertThat(response.getBody()).ignoringCase().contains("<convo>unwelcome</convo>");

        response = tree.listen(welcome, "I think I'm welcome");
        Truth.assertThat(response.getBody()).isEqualTo(oneWay.getBody());
        response = tree.listen(welcome, "But I'll start over");
        Truth.assertThat(response.getBody()).contains(tree.getEndOfConvo());
        response = tree.listen(welcome, "Am I unwelcome?");
        Truth.assertThat(response.getBody()).isEqualTo(otherWay.getBody());

        // unwelcome
        response = tree.listen(unwelcome, "hello there!");
        Truth.assertThat(response.getBody()).ignoringCase().doesNotContain("<convo>welcome</convo>");
        Truth.assertThat(response.getBody()).ignoringCase().contains("<convo>unwelcome</convo>");

        response = tree.listen(unwelcome, "Am I welcome?");
        Truth.assertThat(response.getBody()).ignoringCase().isEqualTo(tree.getNotRecognized());
        response = tree.listen(unwelcome, "Am I unwelcome?");
        Truth.assertThat(response.getBody()).isEqualTo(otherWay.getBody());

    }

    @Test
    void testDualTriggerForbiddance() {
        Creature welcome = new NonPlayerCharacter();
        Creature unwelcome = new NonPlayerCharacter();
        while (welcome.getName().equals(unwelcome.getName())) {
            unwelcome.setName(unwelcome.getName() + "x");
        }
        Truth.assertThat(unwelcome.getName()).isNotEqualTo(welcome.getName());

        ConversationTreeNode start = new ConversationTreeNode(
                "I greet you back " + ConversationContextKey.TALKER_TAGGED_NAME);
        start.addBody("I will test the welcome and the unwelcome both");
        ConversationTree tree = new ConversationTree(start);
        ConversationTreeNode oneWay = new ConversationTreeNode("I am friendly");
        ConversationTreeNode otherWay = new ConversationTreeNode("I am not friendly");

        // order matters!
        ConversationTreeBranch oneBranch = tree.addNode(start.getNodeID(),
                Pattern.compile("\\bboth\\b", Pattern.CASE_INSENSITIVE), oneWay);
        tree.addNode(start.getNodeID(),
                Pattern.compile("\\bboth\\b", Pattern.CASE_INSENSITIVE), otherWay);

        // unwelcome is not welcome to the oneWay
        oneBranch.addRule(ConversationContextKey.TALKER_NAME, Pattern.compile("\\b" + unwelcome.getName() + "\\b"));

        // welcome
        ConversationTreeNodeResult response = tree.listen(welcome, "hello there!");
        Truth.assertThat(response.getBody()).ignoringCase().contains("<convo>both</convo>");
        response = tree.listen(welcome, "You test both?");
        Truth.assertThat(response.getBody()).isEqualTo(oneWay.getBody());

        // unwelcome
        response = tree.listen(unwelcome, "hello there!");
        Truth.assertThat(response.getBody()).ignoringCase().contains("<convo>both</convo>");
        response = tree.listen(unwelcome, "You test both?");
        Truth.assertThat(response.getBody()).isEqualTo(otherWay.getBody());
    }
}
