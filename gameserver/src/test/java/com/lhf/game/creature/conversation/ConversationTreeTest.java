package com.lhf.game.creature.conversation;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.truth.Truth;
import com.google.gson.Gson;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.conversation.ConversationTransformer.ConversationContextKey;
import com.lhf.game.serialization.GsonBuilderFactory;
import com.lhf.server.client.Client.ClientID;

@ExtendWith(MockitoExtension.class)
public class ConversationTreeTest {

    private String basicEmpty = "I have nothing to say to you right now.";
    private ICreature talker;
    private ClientID talkerID;

    @BeforeEach
    void init() {
        this.talker = Mockito.mock(INonPlayerCharacter.class);
        this.talkerID = new ClientID();
    }

    @Test
    void testListenNoStartNode() {
        assertThrows(NullPointerException.class, () -> {
            new ConversationTree(null);
        });
    }

    @Test
    void testIgnoreUngreeted() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);

        ConversationTreeNode node = new ConversationTreeNode(basicEmpty);
        ConversationTree tree = new ConversationTree(node);
        ConversationTreeNodeResult response = tree.listen(talker, "unrecongized words like zaosdff");
        Truth.assertThat(response).isNull();
    }

    @Test
    void testOneTrackConversation() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);
        Mockito.when(this.talker.getName()).thenReturn("Talker Joe");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        ConversationTreeNode start = new ConversationTreeNode(basicEmpty);
        ConversationTree tree = new ConversationTree(start);
        String secondBody = "Yes I am!";
        tree.addNode(start.getNodeID(),
                new ConversationPattern("Are you sure?", "\\bsure\\b.*?", Pattern.CASE_INSENSITIVE),
                new ConversationTreeNode(secondBody));

        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.printString()).isEqualTo(start.getBodyAsString());
        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response.printString()).isEqualTo(secondBody);
    }

    @Test
    void testTwoTrackConversation() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);
        Mockito.when(this.talker.getName()).thenReturn("Talker Joe");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        ConversationTreeNode start = new ConversationTreeNode(basicEmpty);
        ConversationTree tree = new ConversationTree(start);
        String secondBody = "Yes I am!";
        tree.addNode(start.getNodeID(),
                new ConversationPattern("Are you sure?", "\\bsure\\b.*?", Pattern.CASE_INSENSITIVE),
                new ConversationTreeNode(secondBody));
        String thirdBody = "Fine!";
        tree.addNode(start.getNodeID(), new ConversationPattern("Fine!", "^fine\\b!$", Pattern.CASE_INSENSITIVE),
                new ConversationTreeNode(thirdBody));

        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.printString()).isEqualTo(start.getBodyAsString());
        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response.printString()).isEqualTo(secondBody);

        response = tree.listen(talker, "fine!");
        Truth.assertThat(response.printString()).isNotEqualTo(thirdBody);
        Truth.assertThat(response.printString()).isEqualTo(tree.getEndOfConvo());
    }

    @Test
    void testConvoRollover() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);
        Mockito.when(this.talker.getName()).thenReturn("Talker Joe");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        ConversationTreeNode start = new ConversationTreeNode(basicEmpty);
        ConversationTree tree = new ConversationTree(start);
        String secondBody = "Yes I am!";
        tree.addNode(start.getNodeID(),
                new ConversationPattern("Are you sure?", "\\bsure\\b.*?", Pattern.CASE_INSENSITIVE),
                new ConversationTreeNode(secondBody));
        String thirdBody = "Fine!";
        tree.addNode(start.getNodeID(), new ConversationPattern("Fine!", "^fine\\b!$", Pattern.CASE_INSENSITIVE),
                new ConversationTreeNode(thirdBody));

        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.printString()).isEqualTo(start.getBodyAsString());
        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response.printString()).isEqualTo(secondBody);

        response = tree.listen(talker, "fine!");
        Truth.assertThat(response.printString()).isNotEqualTo(thirdBody);
        Truth.assertThat(response.printString()).isEqualTo(tree.getEndOfConvo());

        response = tree.listen(talker, "fine!");
        Truth.assertThat(response.printString()).isEqualTo(thirdBody);
        Truth.assertThat(response.printString()).isNotEqualTo(tree.getEndOfConvo());

        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response.printString()).isNotEqualTo(secondBody);
        Truth.assertThat(response.printString()).isEqualTo(tree.getEndOfConvo());
    }

    @Test
    void testRememberSpot() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);
        Mockito.when(this.talker.getName()).thenReturn("Talker Joe");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        ConversationTreeNode start = new ConversationTreeNode(basicEmpty);
        ConversationTree tree = new ConversationTree(start);
        String secondBody = "Yes I am!";
        ConversationTreeNode secondNode = new ConversationTreeNode(secondBody);
        tree.addNode(start.getNodeID(), new ConversationPattern("sure?", "\\bsure\\b.*?", Pattern.CASE_INSENSITIVE),
                secondNode);
        String thirdBody = "Fine!";
        tree.addNode(secondNode.getNodeID(), new ConversationPattern("fine!", "^fine\\b!$", Pattern.CASE_INSENSITIVE),
                new ConversationTreeNode(thirdBody));

        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.printString()).isEqualTo(start.getBodyAsString());
        response = tree.listen(talker, "zippity doo dah");
        Truth.assertThat(response.printString()).doesNotContain(tree.getEndOfConvo());
        Truth.assertThat(response.printString()).isEqualTo(tree.getNotRecognized());
        response = tree.listen(talker, "what was that again?");
        Truth.assertThat(response.printString()).isEqualTo(start.getBodyAsString());
        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response.printString()).isEqualTo(secondNode.getBodyAsString());
        response = tree.listen(talker, "zippity eh");
        Truth.assertThat(response.printString()).isEqualTo(tree.getNotRecognized());
        response = tree.listen(talker, "what was that again?");
        Truth.assertThat(response.printString()).isEqualTo(secondNode.getBodyAsString());

    }

    @Test
    void testRepeatNode() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);
        Mockito.when(this.talker.getName()).thenReturn("Talker Joe");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        ConversationTreeNode start = new ConversationTreeNode(basicEmpty);
        ConversationTree tree = new ConversationTree(start);
        String secondBody = "Yes I am!";
        tree.addNode(start.getNodeID(), new ConversationPattern("sure?", "\\bsure\\b.*?", Pattern.CASE_INSENSITIVE),
                new ConversationTreeNode(secondBody));
        String thirdBody = "Fine!";
        tree.addNode(start.getNodeID(), new ConversationPattern("fine!", "^fine\\b!$", Pattern.CASE_INSENSITIVE),
                new ConversationTreeNode(thirdBody));

        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.printString()).isEqualTo(start.getBodyAsString());
        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response.printString()).isEqualTo(secondBody);

        response = tree.listen(talker, "what was that again?");
        Truth.assertThat(response.printString()).isEqualTo(secondBody);
    }

    @Test
    void testHightlightNext() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);
        Mockito.when(this.talker.getName()).thenReturn("Talker Joe");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        String body1 = "Hello there new young traveller!";
        ConversationTreeNode start = new ConversationTreeNode(body1);
        ConversationTree tree = new ConversationTree(start);
        String body2 = "Why yes, you are a traveller, are you not?";
        ConversationTreeNode second = new ConversationTreeNode(body2);

        tree.addNode(start.getNodeID(),
                new ConversationPattern("I'm a traveller?", "\\btraveller\\b", Pattern.CASE_INSENSITIVE), second);
        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.print()).contains("<convo colored=\"true\">traveller</convo>");
    }

    @Test
    void testGreetBack() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);
        Mockito.when(this.talker.getName()).thenReturn("Talker Joe");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        ConversationTreeNode start = new ConversationTreeNode("I greet you back")
                .addMetaSignal(ConversationContextKey.TALKER_TAGGED_NAME);
        ConversationTree tree = new ConversationTree(start);

        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.printString()).contains(talker.getName());
    }

    @Test
    void testForbidBranch() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);
        Mockito.when(this.talker.getName()).thenReturn("Talker Joe");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        ICreature unwelcome = Mockito.mock(INonPlayerCharacter.class);
        ClientID id = new ClientID();
        Mockito.when(unwelcome.getClientID()).thenReturn(id);
        Mockito.when(unwelcome.getName()).thenReturn("Unwelcome Bob");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        Truth.assertThat(unwelcome.getName()).isNotEqualTo(talker.getName());

        ConversationTreeNode start = new ConversationTreeNode(
                "I greet you back " + ConversationContextKey.TALKER_TAGGED_NAME);
        start.addBody("I will test the welcome and the unwelcome both");
        ConversationTree tree = new ConversationTree(start);
        ConversationTreeNode oneWay = new ConversationTreeNode("I am friendly");
        ConversationTreeNode otherWay = new ConversationTreeNode("I am not friendly");

        ConversationTreeBranch oneBranch = tree.addNode(start.getNodeID(),
                new ConversationPattern("I'm welcome?", "\\bwelcome\\b", Pattern.CASE_INSENSITIVE), oneWay);
        tree.addNode(start.getNodeID(),
                new ConversationPattern("I'm unwelcome?", "\\bunwelcome\\b", Pattern.CASE_INSENSITIVE), otherWay);

        // unwelcome is not welcome to the oneWay
        oneBranch.addRule(ConversationContextKey.TALKER_NAME,
                new ConversationPattern(unwelcome.getName(), "\\b" + unwelcome.getName() + "\\b"));

        // welcome
        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.printString()).ignoringCase().contains("<convo colored=\"true\">welcome</convo>");
        Truth.assertThat(response.printString()).ignoringCase().contains("<convo colored=\"true\">unwelcome</convo>");

        response = tree.listen(talker, "I think I'm welcome");
        Truth.assertThat(response.printString()).isEqualTo(oneWay.getBodyAsString());
        response = tree.listen(talker, "But I'll start over");
        Truth.assertThat(response.printString()).contains(tree.getEndOfConvo());
        response = tree.listen(talker, "Am I unwelcome?");
        Truth.assertThat(response.printString()).isEqualTo(otherWay.getBodyAsString());

        // unwelcome
        response = tree.listen(unwelcome, "hello there!");
        Truth.assertThat(response.printString()).ignoringCase()
                .doesNotContain("<convo colored=\"true\">welcome</convo>");
        Truth.assertThat(response.printString()).ignoringCase().contains("<convo colored=\"true\">unwelcome</convo>");

        response = tree.listen(unwelcome, "Am I welcome?");
        Truth.assertThat(response.printString()).ignoringCase().isEqualTo(tree.getNotRecognized());
        response = tree.listen(unwelcome, "Am I unwelcome?");
        Truth.assertThat(response.printString()).isEqualTo(otherWay.getBodyAsString());

    }

    @Test
    void testDualTriggerForbiddance() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);
        Mockito.when(this.talker.getName()).thenReturn("Talker Joe");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        ICreature unwelcome = Mockito.mock(INonPlayerCharacter.class);
        ClientID id = new ClientID();
        Mockito.when(unwelcome.getClientID()).thenReturn(id);
        Mockito.when(unwelcome.getName()).thenReturn("Unwelcome Bob");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        Truth.assertThat(unwelcome.getName()).isNotEqualTo(talker.getName());

        ConversationTreeNode start = new ConversationTreeNode(
                "I greet you back " + ConversationContextKey.TALKER_TAGGED_NAME);
        start.addBody("I will test the welcome and the unwelcome both");
        ConversationTree tree = new ConversationTree(start);
        ConversationTreeNode oneWay = new ConversationTreeNode("I am friendly");
        ConversationTreeNode otherWay = new ConversationTreeNode("I am not friendly");

        // order matters!
        ConversationTreeBranch oneBranch = tree.addNode(start.getNodeID(),
                new ConversationPattern("both?", "\\bboth\\b", Pattern.CASE_INSENSITIVE), oneWay);
        tree.addNode(start.getNodeID(), new ConversationPattern("both?", "\\bboth\\b", Pattern.CASE_INSENSITIVE),
                otherWay);

        // unwelcome is not welcome to the oneWay
        oneBranch.addRule(ConversationContextKey.TALKER_NAME,
                new ConversationPattern(unwelcome.getName(), "\\b" + Pattern.quote(unwelcome.getName()) + "\\b"));

        // welcome
        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.printString()).ignoringCase().contains("<convo colored=\"true\">both</convo>");
        response = tree.listen(talker, "You test both?");
        Truth.assertThat(response.printString()).isEqualTo(oneWay.getBodyAsString());

        // unwelcome
        response = tree.listen(unwelcome, "hello there!");
        Truth.assertThat(response.printString()).ignoringCase().contains("<convo colored=\"true\">both</convo>");
        response = tree.listen(unwelcome, "You test both?");
        Truth.assertThat(response.printString()).isEqualTo(otherWay.getBodyAsString());
    }

    @Test
    void testSerialization() {
        ConversationTreeNode start = new ConversationTreeNode(
                "I greet you back " + ConversationContextKey.TALKER_TAGGED_NAME);
        start.addBody("I will test the welcome and the unwelcome both");
        ConversationTree tree = new ConversationTree(start);
        ConversationTreeNode oneWay = new ConversationTreeNode("I am friendly");
        ConversationTreeNode otherWay = new ConversationTreeNode("I am not friendly");

        // order matters!
        ConversationTreeBranch oneBranch = tree.addNode(start.getNodeID(),
                new ConversationPattern("both?", "\\bboth\\b", Pattern.CASE_INSENSITIVE), oneWay);
        tree.addNode(start.getNodeID(), new ConversationPattern("both?", "\\bboth\\b", Pattern.CASE_INSENSITIVE),
                otherWay);

        // unwelcome is not welcome to the oneWay
        oneBranch.addRule(ConversationContextKey.TALKER_NAME,
                new ConversationPattern("badperson", "\\b" + "badperson" + "\\b"));

        ConversationTreeNode oneWaySecond = new ConversationTreeNode("So very friendly!");
        tree.addNode(oneWay.getNodeID(),
                new ConversationPattern("You are friendly?", "\\bfriendly\\b", Pattern.CASE_INSENSITIVE), oneWaySecond);

        Gson gson = GsonBuilderFactory.start().conversation().build();
        String json = gson.toJson(tree);
        System.out.println(json);
        Truth.assertThat(json).ignoringCase().contains("greet");
        Truth.assertThat(json).ignoringCase().contains("test");
        Truth.assertThat(json).ignoringCase().contains("friendly");
        Truth.assertThat(json).ignoringCase().contains("badperson");

        ConversationTree secondTree = gson.fromJson(json, ConversationTree.class);
        Truth.assertThat(secondTree).isInstanceOf(ConversationTree.class);
        Truth.assertThat(secondTree.getTreeName()).isEqualTo(tree.getTreeName());
        Truth.assertThat(secondTree).isEqualTo(tree);

    }

    @Test
    void testMermaid() {
        ConversationTreeNode start = new ConversationTreeNode(
                "I greet you back " + ConversationContextKey.TALKER_TAGGED_NAME);
        start.addBody("I will test the welcome and the unwelcome both");
        ConversationTree tree = new ConversationTree(start);
        ConversationTreeNode oneWay = new ConversationTreeNode("I am friendly");
        oneWay.addPrompt("PROMPT DROP money");
        ConversationTreeNode otherWay = new ConversationTreeNode("I am not friendly");

        // order matters!
        ConversationTreeBranch oneBranch = tree.addNode(start.getNodeID(),
                new ConversationPattern("both?", "\\bboth\\b", Pattern.CASE_INSENSITIVE), oneWay);
        tree.addNode(start.getNodeID(), new ConversationPattern("both?", "\\bboth\\b", Pattern.CASE_INSENSITIVE),
                otherWay);

        // unwelcome is not welcome to the oneWay
        oneBranch.addRule(ConversationContextKey.TALKER_NAME,
                new ConversationPattern("badperson", "\\b" + "badperson" + "\\b"));

        ConversationTreeNode oneWaySecond = new ConversationTreeNode("So very friendly!");
        tree.addNode(oneWay.getNodeID(),
                new ConversationPattern("you are friendly?", "\\bfriendly\\b", Pattern.CASE_INSENSITIVE), oneWaySecond);

        String mermaid = tree.toMermaid(false);
        System.out.println(mermaid);
        Truth.assertThat(mermaid).ignoringCase().contains("greet");
        Truth.assertThat(mermaid).ignoringCase().contains("test");
        Truth.assertThat(mermaid).ignoringCase().contains("friendly");
        Truth.assertThat(mermaid).ignoringCase().contains("badperson");
    }
}
